// Copyright (C) 2013 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.googlesource.gerrit.plugins.forcedraft;

import com.google.gerrit.common.data.GlobalCapability;
import com.google.gerrit.extensions.annotations.CapabilityScope;
import com.google.gerrit.extensions.annotations.RequiresCapability;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.PatchSet;
import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.index.change.ChangeIndexer;
import com.google.gerrit.sshd.CommandMetaData;
import com.google.gerrit.sshd.SshCommand;
import com.google.gwtorm.server.AtomicUpdate;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import org.eclipse.jgit.lib.Config;
import org.kohsuke.args4j.Argument;

@RequiresCapability(value = GlobalCapability.ADMINISTRATE_SERVER, scope = CapabilityScope.CORE)
@CommandMetaData(name = "force-draft", description = "changes patch set to draft")
public class ForceDraft extends SshCommand {

  private static final String CHANGE_SECTION = "change";

  private static final String ALLOW_DRAFT = "allowDrafts";

  /** The PatchSet specified by argument. */
  private PatchSet patchSet;

  /** Parent Change for patchSet. */
  private Change parentChange;

  @Inject private Provider<ReviewDb> dbProvider;

  @Inject private @GerritServerConfig Config config;

  @Inject private ChangeIndexer changeIndexer;

  @Argument(
      index = 0,
      required = true,
      metaVar = "{CHANGE,PATCHSET}",
      usage = "<change, patch set> to be changed to draft")
  private void addPatchSetId(final String token) {
    try {
      patchSet = parsePatchSet(token);
      parentChange = getParentChange();
    } catch (UnloggedFailure e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    } catch (OrmException e) {
      throw new IllegalArgumentException("database error", e);
    }
  }

  /**
   * Gets parent Change for patchSet.
   *
   * @return The parent Change
   * @throws OrmException
   */
  private Change getParentChange() throws OrmException {
    Change parentChange = dbProvider.get().changes().get(patchSet.getId().getParentKey());
    return parentChange;
  }

  /**
   * Parses a string formatted as <Change id>,<PatchSet number>
   *
   * @param changePatchSet
   * @return The PatchSet specified by change_patchSet string.
   * @throws UnloggedFailure
   * @throws OrmException
   */
  private PatchSet parsePatchSet(String changePatchSet) throws UnloggedFailure, OrmException {
    if (changePatchSet.matches("^[1-9][0-9]*,[1-9][0-9]*$")) {
      final PatchSet.Id patchSetId;
      try {
        patchSetId = PatchSet.Id.parse(changePatchSet);
      } catch (IllegalArgumentException e) {
        throw new UnloggedFailure(1, "\"" + changePatchSet + "\" is not a valid patch set");
      }
      final PatchSet patchSet = dbProvider.get().patchSets().get(patchSetId);
      if (patchSet == null) {
        throw new UnloggedFailure(1, "\"" + changePatchSet + "\" no such patch set");
      }
      return patchSet;
    }
    throw new UnloggedFailure(1, "\"" + changePatchSet + "\" is not a valid patch set");
  }

  /**
   * Sends message to stdout with new line.
   *
   * @param message
   */
  private void sendUserInfo(String message) {
    stdout.print(message + "\n");
  }

  /**
   * Gets a string representation of the Change.Status.
   *
   * @param changeStatus
   * @return The name of the Change.Status.
   */
  private String getStatusName(Change.Status changeStatus) {
    String statusName = changeStatus.toString().toLowerCase() + ".";
    return statusName;
  }

  /**
   * Sets PatchSet specified by argument as Draft if parentChange has status NEW.
   *
   * @return The updated PatchSet.
   * @throws OrmException
   */
  private PatchSet setPatchSetAsDraft() throws OrmException {
    final PatchSet updatedPatchSet =
        dbProvider
            .get()
            .patchSets()
            .atomicUpdate(
                patchSet.getId(),
                new AtomicUpdate<PatchSet>() {
                  @Override
                  public PatchSet update(PatchSet patchset) {
                    patchset.setDraft(true);
                    sendUserInfo("Patch set successfully set to draft.");
                    return patchset;
                  }
                });
    return updatedPatchSet;
  }

  /**
   * Returns all PatchSets in Change with Id = changeId.
   *
   * @param changeId
   * @return A Iterable<PatchSet> view of all Patch sets in change.
   * @throws OrmException
   */
  private Iterable<PatchSet> getPatchSetsForChange(Change.Id changeId) throws OrmException {
    return dbProvider.get().patchSets().byChange(changeId);
  }

  /**
   * Checks if every PatchSet in Change, with Id = changeId, is drafts.
   *
   * @param changeId
   * @return Returns true if all PatchSets in Change are drafts.
   * @throws OrmException
   */
  private boolean isAllPatchSetsInChangeDrafts(Change.Id changeId) throws OrmException {
    boolean isAllDrafts = true;
    Iterable<PatchSet> patchSets = getPatchSetsForChange(changeId);
    for (PatchSet patchset : patchSets) {
      if (!patchset.isDraft()) {
        isAllDrafts = false;
        break;
      }
    }
    return isAllDrafts;
  }

  /**
   * Updates parentChange to draft if every Patch set in Change is Draft.
   *
   * @return the updated change
   * @throws OrmException
   */
  private Change updateChange() throws OrmException {
    final Change updatedChange =
        dbProvider
            .get()
            .changes()
            .atomicUpdate(
                parentChange.getId(),
                new AtomicUpdate<Change>() {
                  @Override
                  public Change update(Change change) {
                    boolean shouldBeDraft;
                    try {
                      shouldBeDraft = isAllPatchSetsInChangeDrafts(parentChange.getId());
                    } catch (OrmException e) {
                      sendUserInfo("Unable to check if every patch set in change is draft.");
                      shouldBeDraft = false;
                    }
                    if (shouldBeDraft) {
                      change.setStatus(Change.Status.DRAFT);
                      sendUserInfo("Every patch set in change is draft, change set to draft.");
                    }
                    return change;
                  }
                });
    return updatedChange;
  }
  /**
   * Updates PatchSet and, if applicable, parent Change.
   *
   * @throws OrmException if an error occur while updating the change in the DB.
   * @throws IOException if an error occur while indexing the change.
   */
  private void updatePatchSet() throws OrmException, IOException {
    Change.Status changeStatus = parentChange.getStatus();
    if (changeStatus == Change.Status.NEW) {
      setPatchSetAsDraft();
      changeIndexer.index(dbProvider.get(), updateChange());
    } else {
      sendUserInfo("Unable to set patch set as draft, change is " + getStatusName(changeStatus));
    }
  }

  private boolean isDraftWorkFlowDisabled() {
    boolean draftsAllowed = config.getBoolean(CHANGE_SECTION, ALLOW_DRAFT, true);
    return !draftsAllowed;
  }

  @Override
  public void run() throws UnloggedFailure, Failure, Exception {
    if (isDraftWorkFlowDisabled()) {
      sendUserInfo("Draft workflow disabled in gerrit.config, unable to set to draft.");
    } else if (patchSet.isDraft()) {
      sendUserInfo("Patch set is already draft.");
    } else {
      updatePatchSet();
    }
  }
}
