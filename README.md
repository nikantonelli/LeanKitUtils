# Overview

Currently this is limited to an exporter/importer app. The same app will do both directions independently. Future development may make it do both at once, i.e 'simultaneous transfer'

# Setup

The app does its work through the medium of an Excel spreadsheet. This has more flexibility that using a CSV file as I can do stuff to implement parent/child relationships fairly easily. In addition, comments, descriptions, etc. can cause issues if they contain the "," character

The app may also require you to have read/write access on the directory you are running it from. This is to store the attachment files if you have asked for them to be exported.

The basic requirement to fire off the app is a file with a single sheet in it. The columns of the sheet must be: 'direction', 'url', 'username', 'password', 'apiKey' and 'boardId'. The first row found with 'src' as the entry under direction will be used by the exporter. The first row found with 'dst' will be used by the importer.

You need to provide either a username/password pair or apiKey that is valid for the url you wish to access. 

To get the board id, log into the system at the url, navigate to the board of interest and take the digit string from the end of the URL in the browser address bar.

# Command Line Options
Option | Argument | Description 
------ | -------- | -----------
-f | \<file\> | Name of the Xlsx file to use for reading/writing
-x | \<level\> |  Output levels of debug statements: 0 = Errors, 1 = +Info, 2 = +Warnings, 3 = +Debug, 4 = +Verbose
-i |  | Run importer only
-e |  | Run exporter only 
-t |  | Run importer and exporter sequentially
-g | \<group\> | Mark exported items with this groupId ready for selection on import. Select only items marked with this group for import
-O |  | Include _Older_ archived items during export
-A |  | Include _Attachments_ in export - these get placed in your current working directory 
-T |  | Include _Tasks_ in export
-C |  | Include  _Comments_ in export
-S |  | Include a comment in export containing link to original _Source_
 
# Features and Usage
 
The exporter will create sheets in the Xlsx file that correspond to the boardId that will be needed by the importer (if you progress to that stage). The exporter does not export any history information and takes a snapshot of what is there right now.
 
You can re-use the same spreadsheet to export multiple boards as each one is saved under a different sheet name

If you are running the export in order to run the importer, you must copy (or rename) the sheet entitled Changes_\<boardid\> to a sheet called just Changes. 
 
If you want to merge boards together into one destination board, you can concatenate multiple changes sheet together, leaving the board item sheets as they are. E.g. merge sheets called "Changes_1598676317" and "Changes_1606150498" (created by the exporter) into one sheet called "Changes" and then run the importer. Remember! You will have issues with Lanes if the layouts of the boards are imcompatible. All items that cannot be put into a correct lane will end up in the default drop lane - this can get messy. To recover, you can delete all the items in the default drop lane that aren't supposed to be there and set the value in the Group column in the Changes sheet to something memorable (e.g. 99) for those items you want to recreate and modify. Then rerun the importer with the -g option with that group number.
 
