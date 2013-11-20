This plugin enables admin-user to easily change PatchSet to draft.

Sometimes Git-administrators are forced to remove PatchSets for legal,
or other reason. Today this is done by setting the PatchSet as Draft in db.
This procedure is troublesome and error-prone due to the human
factor.
This plugin enables administrators to perform the procedure with a simple
ssh-command.

If all PatchSets of parent change are drafts, parent Change is
also set to draft.
