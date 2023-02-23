## Overview

Currently this is limited to an exporter/importer app. The same app will do both directions independently. Future development may make it do both at once, i.e 'simultaneous transfer' but, for now, you need to provide both the -e and -i options to do a _transfer_

With this tool, you can make a backup of the current state of cards on your board (-e) - recreate a copy of the cards on the same, or different, board (-i) - merge cards from boards together into a single destination board. 

It will copy current cards (with option to include older archived) with tasks, comments and attachments (if indicated), as well as marking the source of the copy by adding an additional comment in the destination card. AssignedUsers are preserved if the same login name exists on the source and destination.

If you wish, you can create your own spreadsheet and import a particular set of cards - handy if you want to have 'template' sets you want to tailor to some specific use-case, e.g a repeatable set of actions for a scrum team.

## Changes

- Version 2.0 Import of large sections of code updates from 'replicator' utility
 1. Reworked command line options
 2. New board archive/create/delete/update features
 3. Delete of all cards on a destination board (can be done in isolation if required)
- Version 1.2 refactored the whole thing and added a few utilities. It also has two breaking changes: 
 1. in the layout of the spreadsheet config page.
 2. Command line options are changed. BEWARE! unless this is the first time you are using this app.
- Version 1.1 added a way to maintain relationships between cards on different boards (with some caveats!)

## Setup

The app does its work through the medium of an Excel spreadsheet. This has more flexibility that using a CSV file as I can do stuff to implement parent/child relationships fairly easily. In addition, comments, descriptions, etc. can cause issues if they contain the "," character in CSV files.

If you have the Excel file open when this app is trying to run, it may fail whilst trying to save its updates. Please ensure to close the Excel file after edits. This is an Excel/filesystem issue and relates to how the OS keeps track of reads/writes to (auto-save,open, etc.) files.

The app may also require you to have read/write access on the directory you are running it from. This is to store the attachment files if you have asked for them to be exported.

The basic requirement to fire off the app is a file with a single sheet in it. The columns of the sheet are in the section below and there should be an example in this github repo.

You need to provide an apiKey that is valid for the url you wish to access. You can generate this by clicking on your Avatar in the top-right hand corner and selecting "API Tokens" from the menu.

## Command Line Options
Option | Argument | Description 
------ | -------- | -----------
-f | \<file\> | (String) Name of the Xlsx (f)ile to use for reading/writing
-x | \<level\> | (Integer) Output levels of debug statements: 0 = Errors, 1 = +Warnings, 2 = +Info, 3 = +Debug, 4 = +Verbose
-i |  | Run (i)mporter 
-e |  | Run (e)xporter  
-r |  | (r)emake target boards by archiving old and adding new
-R |  | (R)emove target boards completely (delete)
-l |  | Replace (l)ayout on target board
-d |  | (d)elete all cards on the target boards
-P |  | Ex(P)ort read-only fields (Not Imported!)
-c |  | Run in (c)ompare mode (Needs previous export data)
-m |  | Lane to modify unwanted cards with (for compare only)
-t |  | Follow external links (from (t)asktop integration to Jira or ADO) and delete pair
-v |  | (BEWARE!) change le(v)els on target system to match source system. Used in conjunction with -l or -r, not independently.
-g | \<group\> | (Integer)  Mark exported items with this (g)roupId ready for selection on import. Select only items marked with this group for import
-O |  | Include _Older_ archived items during export
-A |  | Include _Attachments_ in export/import - these get placed in your current working directory 
-T |  | Include _Tasks_ in export/import
-C |  | Include  _Comments_ in export/import
-S |  | Include a comment in export containing link to original _Source_ (will not get imported if -C not used)
-P |  | Export some Read Only fields (which will not be imported)
 
## Features and Usage
 
* The exporter will create sheets in the Xlsx file that correspond to the boardId that will be needed by the importer (if you progress to that stage). If you run with only the -e option, then there are two sheets created per board: "C_\<boardName\>" and "<boardName\>".
* The exporter does not export any history (i.e. dates of changes, createdBy, ActualFinish, etc., etc.) information and takes a snapshot of what is there right now. As this app is all about recreating new items afresh, all that data is irrelevant. To get those dates, please use the standard in-built exporter (to csv).
* You can re-use the same spreadsheet to export multiple boards (with -e option only).
* If you are running the export in order to run the importer some time later, you must update the src/dst boardIds in the correct sequence. There are a few scenarios:
  * If you want to just export, then the Config sheet only need contain one line containing the 'src' info
  * If you want to import, then just the 'dst' info is needed
  * If you want to copy multiple boards from one system to another and there are parent/child relationships between cards on different boards, then you need to ensure that you export in the correct order. This is so the program can find the items and make the equations up so that the parent links are recreated properly on import. An example of this is when you have a Portfolio board with Initiatives on that have Epics as children on an Agile Release Train board, that itself has Features as children on a Team board (that may or may not have stories). You must export in the order, Portfolio, ART, Team. 
* If you want to merge boards together into one destination board, you can concatenate multiple changes sheet together, leaving the board item sheets as they are. E.g. copy sheets called "C_My_Team_Board" and "C_Your_Team_Board" (created by the exporter) into one sheet called "C_Combined" and then set the src board name to "Combined" to run the importer. Remember! You will have issues with Lanes if the layouts of the boards are incompatible.  You can use the -l to overwrite the source layout onto the destination.

WIP limits are overridden automatically with a default message. This is a fixed _feature_

* All items that cannot be put into a correct lane will end up in the default drop lane - this can get messy. To recover, you can delete all the items in the default drop lane that aren't supposed to be there and set the value in the Group column in the Changes sheet to something memorable (e.g. 99) for those items you want to recreate and modify. Then rerun the importer with the -g option with that group number.
* To run both the importer and exporter sequentially, for example, you can use the following command line:
 
java -jar lkutility\target\lkutility-1.3-jar-with-dependencies.jar -f "file.xlsx" -e -i  -A -T -S -C
 
* To get an example spreadsheet of what the importer requires, you can run the export (only, using -e) on a board that has parent/child, attachment, comment, etc., data already set up.
* To get an idea of the progress that the exporter/importer is making, use the option "-x 2". You can increase that to 3 or 4 if you want more info on what the program is doing.
* The importer does not check validity of data before performing its work. Any incorrect data might cause the card to not be imported as expected, i.e. incorrect data is ignored where possible.
* Assigned Users are matched by email address and if they exist in the destination are automatically added to the destination board

## Spreadsheet Row Formats

### Config
The columns listed in the first row of the Config sheet must be: 'srcUrl', 'srcBoardName',  'srcApiKey', 'dstUrl', dstBoardName', 'dstApiKey', 'Import Ignore', 'ADO User', ' ADO Token', 'JIRA User', 'JIRA Key.

### Changes

The columns in the Changes sheet need to be listed in the first row and are: "Group", "Item Row", "Action", "Field", "Value".

The Changes sheet can contain either 'Create' rows or 'Modify' rows. 

Field and Value cells are only used when the Action cell is set to Modify.
Action cells set to Create instruct the importer to reference the data in the sheet/row named in the Item Sheet/Item Row cell pair.
The Group is compared to that on the command line (-g option) and only those rows matching will be used.

Entries in the Changes sheets are instructions to the importer on what to do.

### Board Sheets

Board sheets are normally reference by the board name. However, if you are making your own set of import data, it can be whatever you want within the bounds of what Excel will allow (30 chars). Board sheets list the data for the items to be re-created/imported.

For the importer to function it requires two columns named "ID" and "srcID" (both text based) and at least the "title" of the card to be created as this is a mandatory field (LeanKit defined). Other columns can be any number of supported fieldnames as listed below. If you duplicate fieldnames, the last-found field value will be used. The fieldnames to be set should be listed in the first row.

To import Custom Fields, see below.
 
## Parent/Child Relationships
 
The use of the spreadsheet allows the indirect logging of the parent/child relationships. This is useful when you don't yet know the Id of the cards in the destination board. A 'Modify' row in the Changes sheet will allow you to point a child to a parent item by using a FORMULA in the cell.
 
## OnScreen positioning and Indexes
 
The priority of a card is normally set as an index of a card with zero being at the top of the screen. The upshot of this is the importer may attempt to set the Index to some value that may not yet be valid (as all the cards have not yet been created) if you do them in the wrong order.

If you are manually creating the importer spreadsheet, you will need to bear this in mind. The exporter will re-order the indexes appropriately for you, instead of using the default order of: last card accessed comes first.

## Assigned Users on Import

If your destination system does not have the correct users set up (with access to the board), the users are ignored. The tool tries to match the "username" which is usually of emailAddress format.

The importer will take the spreadsheet field as a comma separated list of users.

## Lanes and WIP Limits

Lanes hierarchy is represented as a set of strings separated by the "^" character
To add/move a card to a lane which might cause the WIP limit to be exceeded, add an override comment to the lane field. To do this, add a "`" and then the override comment itself,
e.g:

"Backlog^Next Sprint Backlog^Committed`Expedited" 

will move the card to the sub-lane Committed under "Next Sprint Backlog" which is under "Backlog" and add the wipOverrideComment of "Expedited"

## Supported fields

The exporter will recognise the writeable fields. All read-only are ignored. Some fields are represented in the spreadsheet through the use of pseudo-fieldnames, e.g: "Parent" or "Task". These are translated by the importer into the relevant fields.

Fields that are valid for an item in a board sheet (connected to a 'Create' in the Changes sheet) row are:

* assignedUsers
* blockReason
* color
* comment
* customIcon
* description
* externalLink
* index
* lane
* plannedFinish
* plannedStart
* prority
* size
* tags
* title
* type

## Pseudo Fields

* Parent

This field can be only used as part of a "Modify" line in the Changes sheet and not in the board sheet. The value in the field needs to be an equation that points to the cell that will hold the Id of the parent (e.g. ='My Team Board'!A6 )

* Task

This field can be only used as part of a "Modify" line in the Changes sheet and not in the board sheet. The value in the field needs to be an equation that points to the cell that will hold the Id of the child task (e.g. ='My Team Board'!A8 )

## Custom Fields

Custom fields get listed in the export spreadsheet as would built-in fields. The importer checks all fieldnames against standard ones and then against custom fieldnames and decides what to do accordingly. If still unknown, then the field is ignored.
