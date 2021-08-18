# Overview

Currently this is limited to an exporter/importer app. THe same app will do both direction independently. Future development may make it do both at once, i.e 'transfer'

# Setup

The app does its work through the medium of an Excel spreadsheet. This has more flexibility that using a CSV file as I can do stuff to implement parent/child relationships fairly easily. In addition, comments, descriptions, etc. can cause issues if they contain the "," character

The app may also require you to have read/write access on the directory you are running it from. This is to store the attachment files if you have asked for them to be exported.

The basic requirement to fire off the app is a file with a single sheet in it. The columns of the sheet must be: 'direction', 'url', 'username', 'password', 'apiKey' and 'boardId'. The first row found with 'src' as the entry under direction will be used by the exporter. The first row found with 'dst' will be used be the importer.

You need to provide either a username/password pair or apiKey that is valid for the url you wish to access. 

To get the board id, log into the system at the url, navigate to the board of interest and take the digit string from the end of the URL in the browser address bar.

The exporter will create sheets in the Xlsx file that correspond to the boardId that will be needed by the importer (if you progress to that stage). The exporter does not export any history information and takes a snapshot of what is there right now.

# Command Line Options
Option | Argument | Description 
------ | -------- | -----------
-f | \<file\> | Name of the Xlsx file to use for reading/writing
-x | \<level\> |  Output levels of debug statements: 0 = Errors, 1 = +Info, 2 = +Warnings, 3 = +Debug, 4 = +Verbose
-i |  | Run importer only
-e |  | Run exporter only 
-t |  | Run importer and exporter together (Currently NOT IMPLEMENTED and does nothing)
-g | \<group\> | Mark exported items with this groupId ready for selection on import. Select only items marked with this group for import
-oo |  | Include older archived items during export
-oa |  | Include attachments for export - these get placed in your current working directory 
-ot |  | Export tasks (does not export task attachments or comments! ....yet) 
-oc |  | Include comments in export
 
