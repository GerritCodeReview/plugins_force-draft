@PLUGIN@
==============

NAME
----
@PLUGIN@ - Force patchset to draft.

SYNOPSIS
--------
```
ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ <Change, Patchset>
```

DESCRIPTION
-----------
Forces specified patchset to 'draft' state in order to help
administrators enable users to remove unwanted patchsets themselves.
If all patchsets of a change are set to 'draft', the change is also set
to 'draft'.
Since this command only sets the patchset, or patchset and change, to
'draft' state, the actual deletion is performed by the user leaving the
administrators free from responsibility.

ACCESS
------
Caller must be a member of the privileged 'Administrators' group.

EXAMPLES
--------
Change status of patchset 3 of change 1234 to 'draft':
```
ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ 1234,3
```
