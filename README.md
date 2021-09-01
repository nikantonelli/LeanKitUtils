## Overview

Currently this is limited to an exporter/importer app. The same app will do both directions independently. Future development may make it do both at once, i.e 'simultaneous transfer'

With this tool, you can make a backup of the current state of cards on your board - recreate a copy of the cards on the same, or different, board - merge boards together into a single destination board. 

It will copy current cards (with option to include archived) with tasks, comments and attachments (if indicated), as well as marking the source of the copy by adding an additional comment in the destination card. AssignedUsers are preserved if the same login name exists on the source and destination.

If you wish, you can create your own spreadsheet and import a particular set of cards - handy if you want to have 'template' sets you want to tailor to some specific use-case, e.g a repeatable set of actions for a scrum team.

## Setup

The app does its work through the medium of an Excel spreadsheet. This has more flexibility that using a CSV file as I can do stuff to implement parent/child relationships fairly easily. In addition, comments, descriptions, etc. can cause issues if they contain the "," character in CSV files.

The app may also require you to have read/write access on the directory you are running it from. This is to store the attachment files if you have asked for them to be exported.

The basic requirement to fire off the app is a file with a single sheet in it. The columns of the sheet must be: 'direction', 'url', 'username', 'password', 'apiKey' and 'boardId'. The first row found with 'src' as the entry under direction will be used by the exporter. The first row found with 'dst' will be used by the importer.

You need to provide either a username/password pair or apiKey that is valid for the url you wish to access. 

To get the board id, log into the system at the url, navigate to the board of interest and take the digit string from the end of the URL in the browser address bar.

## Command Line Options
Option | Argument | Description 
------ | -------- | -----------
-f | \<file\> | Name of the Xlsx file to use for reading/writing
-x | \<level\> |  Output levels of debug statements: 0 = Errors, 1 = +Warnings, 2 = +Info, 3 = +Debug, 4 = +Verbose
-i |  | Run importer only
-e |  | Run exporter only 
-t |  | Run importer and exporter sequentially
-g | \<group\> | Mark exported items with this groupId ready for selection on import. Select only items marked with this group for import
-O |  | Include _Older_ archived items during export
-A |  | Include _Attachments_ in export/import - these get placed in your current working directory 
-T |  | Include _Tasks_ in export/import
-C |  | Include  _Comments_ in export/import
-S |  | Include a comment in export containing link to original _Source_ (will not get imported if -C not used)
 
## Features and Usage
 
The exporter will create sheets in the Xlsx file that correspond to the boardId that will be needed by the importer (if you progress to that stage). The exporter does not export any history (i.e. dates of changes, createdBy, ActualFinish, etc., etc.) information and takes a snapshot of what is there right now. As this app is all about recreating new items afresh, all that data is irrelevant. To get those dates, please use the standard in-built exporter (to csv).
 
You can re-use the same spreadsheet to export multiple boards as each one is saved under a different sheet name

If you are running the export in order to run the importer some time later, you must copy (or rename) the sheet entitled Changes_\<boardid\> to a sheet called "Changes". 
 
If you want to merge boards together into one destination board, you can concatenate multiple changes sheet together, leaving the board item sheets as they are. E.g. merge sheets called "Changes_1598676317" and "Changes_1606150498" (created by the exporter) into one sheet called "Changes" and then run the importer. Remember! You will have issues with Lanes if the layouts of the boards are incompatible. 

I will say it again in case you missed it: YOU MIGHT WANT TO MAKE SURE THE BOARD LAYOUT IS THE SAME.

and: DO NOT PUT ANY LANE WIP LIMITS IN PLACE ON THE DESTINATION BOARD UNLESS YOU HAVE CORRECTLY ADDED AN OVERRIDE COMMENT. REMOVE AND REPLACE AFTER IMPORTING

All items that cannot be put into a correct lane will end up in the default drop lane - this can get messy. To recover, you can delete all the items in the default drop lane that aren't supposed to be there and set the value in the Group column in the Changes sheet to something memorable (e.g. 99) for those items you want to recreate and modify. Then rerun the importer with the -g option with that group number.
 
To run it, for example, you can use the following command line:
 
java -jar lkutility\target\lkutility-1.0-jar-with-dependencies.jar -f "file.xlsx" -t  -A -T -S -C
 
To get an example spreadsheet of what the importer requires, you can run the export (only, using -e) on a board that has parent/child, attachment, comment, etc., data already set up.

To get an idea of the progress that the exporter/importer is making, use the option "-x 3".
 
## Parent/Child Relationships
 
The use of the spreadsheet allows the indirect logging of the parent/child relationships. This is useful when you don't yet know the Id of the cards in the destination board. A 'Modify' row in the Changes sheet will allow you to point a child to a parent item by using a FORMULA in the cell.
 
## OnScreen positioning and Indexes
 
The priority of a card is normally set as an index of a card with zero being at the top of the screen. The upshot of this is the importer may attempt to set the Index to some value that may not yet be valid (as all the cards have not yet been created) if you do them in the wrong order.

If you are manually creating the importer spreadsheet, you will need to bear this in mind. The exporter will re-order the indexes appropriately for you, instead of using the default order of: last card accessed comes first.

## Assigned Users on Import

If your destination system does not have the correct users set up (with access to the board), the users are ignored. The tool tries to match the "username" which is usually of emailAddress format.

The importer will take the spreadsheet field as a comma separated list of users.

## Lane WIP Limits

To add/move a card to a lane which might cause the WIP limit to be exceeded, add an override comment to the lane field. To do this, add a "," and then the override comment itself,
e.g:

"Backlog|Next Sprint Backlog|Committed,Expedited" 

will move the card to the sub-lane Committed under "Next Sprint Backlog" which is under "Backlog" and add the wipOverrideComment of "Expedited"
