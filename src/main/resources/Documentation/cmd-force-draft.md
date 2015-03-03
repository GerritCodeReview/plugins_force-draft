@PLUGIN@ start
==============

NAME
----
force-draft force-draft - Force patchset to draft.

SYNOPSIS
--------
```
ssh -p @SSH_PORT@ @SSH_HOST@ force-draft force-draft <Change, Patchset>
```

DESCRIPTION
-----------
Forces specified patchset to 'draft' state.
If all patchsets of a change is set to 'draft', the change is also set
to 'draft'.
To help administrators enable users to remove unwanted patchsets.
Since this command only sets the patchset to 'draft' state, the actual
deletion is performed by the user leaving the administrators free from
responsibility.

ACCESS
------
Caller must be a member of the privileged 'Administrators' group.

EXAMPLES
--------
Change status of patchset 3 of change 1234 to 'draft':
```
ssh -p @SSH_PORT@ @SSH_HOST@ force-draft force-draft 1234,3
```
