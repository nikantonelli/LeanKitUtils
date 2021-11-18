## How To: Copy a set of boards representing a heirarchy

In some cases, you may want to make a copy of more than one board from one subscription to another.
This app can do it, but there is a caveat.
* The parent/child relationships need to be linear and not circular in any way.

The relationships can be in a tree but must have no links across from branch to branch unless they only occur in one direction and can be sequenced in simple steps
                      a
                    /   \
                   b     c
                 /  \   / \
                 d  e  f  g
        
If board d has children in board e,f or g, and board f has children in g, then that can be sequenced OK. If board f has children in d as well as d having children in f, then that is not possible to recreate.

The problem should not occur that often with a SAFe style of arranging cards and boards. In the scenario above, the Portfolio is board a, the ART boards are b and c, the team boards are d,e,f and g. To get the info into the spreadsheet in the correct way, you would most likely need to export in this order: a,b,c,d,e,f,g However, if f has children in d, you would need to use this order: a,b,c,f,d,e,g. This sequencing is to make sure that the parent is already present on a sheet in the spreadsheet and can be found prior to the export of the child card.

From version 1.1, when you are importing, you need to set the src.boardId to the data you want to take and import. The app uses the boardId to find the correct changes sheet.

Steps to copy boards:

* Set src.boardId to boardA and run export
* Set src.boardId to boardB and run export
* Create newBoardA
* Create newBoardB
* Set src.boardId to boardA, set dst.boardId to newBoardA and run import
* Set src.boardId to boardB, set dst.boardId to newBoardB and run import