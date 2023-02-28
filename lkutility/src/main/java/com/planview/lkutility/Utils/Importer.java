package com.planview.lkutility.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.json.JSONObject;

import com.planview.lkutility.Leankit.Board;
import com.planview.lkutility.Leankit.BoardUser;
import com.planview.lkutility.Leankit.Card;
import com.planview.lkutility.Leankit.CustomField;
import com.planview.lkutility.Leankit.CustomIcon;
import com.planview.lkutility.Leankit.Lane;
import com.planview.lkutility.Leankit.User;
import com.planview.lkutility.System.ChangesColumns;
import com.planview.lkutility.System.ColNames;
import com.planview.lkutility.System.Debug;
import com.planview.lkutility.System.InternalConfig;
import com.planview.lkutility.System.SupportedXlsxFields;

public class Importer {
	Debug d = new Debug();

	InternalConfig cfg = null;

	public Importer(InternalConfig config) {
		cfg = config;
		d.setLevel(cfg.debugLevel);
		XlUtils.d.setLevel(cfg.debugLevel);
	}

	public void go() {

		d.p(Debug.ALWAYS, "Starting Import to \"%s\" at: %s\n", cfg.destination.getBoardName(), new Date());
		/**
		 * cfg might contain the sheet info for the importer if it came from the
		 * exporter directly
		 */

		if (cfg.changesSheet == null) {
			cfg.changesSheet = cfg.wb.getSheet(XlUtils.validateSheetName(InternalConfig.CHANGES_SHEET_NAME + cfg.source.getBoardName()));
		}

		if (null == cfg.changesSheet) {
			d.p(Debug.ERROR, "Cannot find required Changes sheet in file: %s\n", cfg.xlsxfn);
			System.exit(-23);
		}
		ChangesColumns cc = XlUtils.checkChangeSheetColumns(cfg.changesSheet);
		if (cc == null) {
			System.exit(-24);
		}

		// Find all the change records for today
		Iterator<Row> row = cfg.changesSheet.iterator();
		ArrayList<Row> todaysChanges = new ArrayList<Row>();

		// Nw add the rows of today to an array so we can give an info message

		row.next(); // Skip first row with headers
		while (row.hasNext()) {

			Row tr = row.next();
			if (tr.getCell(cc.group) != null) {
				if (tr.getCell(cc.group).getNumericCellValue() == cfg.group) {
					todaysChanges.add(tr);
				}
			}
		}

		if (todaysChanges.size() == 0) {
			d.p(Debug.INFO, "No actions to take for group %d\n", cfg.group);
			return;
		} else {
			d.p(Debug.INFO, "%d actions to take for group %d\n", todaysChanges.size(), cfg.group);
		}
		// Now scan through the changes doing the actions
		Iterator<Row> cItor = todaysChanges.iterator();
		Row item = null;
		while (cItor.hasNext()) {
			Row change = cItor.next();
			// Get the item that this change refers to
			// First check the validity of the info
			if ((change.getCell(cc.row) == null) || (change.getCell(cc.action) == null)) {
				d.p(Debug.WARN, "Cannot decode change info in row \"%d\" - skipping\n", change.getRowNum());
				continue;
			}

			// In case the changes get repositioned, we use the formula to keep track of
			// where the row 'actually' is
			// Excel updates the formula for us if the user edits the changes
			String cf = change.getCell(cc.row).getCellFormula();
			CellReference ca = new CellReference(cf);
			XSSFSheet iSht = cfg.wb.getSheet(ca.getSheetName());
			item = iSht.getRow(ca.getRow());

			Integer idCol = XlUtils.firstColumnFromSheet(iSht, ColNames.ID);
			Integer titleCol = XlUtils.firstColumnFromSheet(iSht, "title");
			Integer typeCol = XlUtils.firstColumnFromSheet(iSht, "type");

			if ((idCol == null) || (titleCol == null)) {
				d.p(Debug.WARN, "Cannot locate \"ID\" and \"title\" columns needed in sheet \"%s\" - skipping\n",
						iSht.getSheetName());
				continue;
			}

			// Check title is present for a Create
			if ((change.getCell(cc.action).getStringCellValue().equals("Create"))
					&& ((item.getCell(titleCol) == null) || (item.getCell(titleCol).getStringCellValue().isEmpty()))) {
				d.p(Debug.WARN,
						"Required \"title\" column/data missing in sheet \"%s\", row: %d for a Create - skipping\n",
						iSht.getSheetName(), item.getRowNum());
				continue;
			}

			if ((typeCol == null) || (item.getCell(typeCol) == null)) {
				d.p(Debug.WARN, "Cannot locate \"type\" column on row:  %d  - using default for board\n",
						item.getRowNum());
			}
			String field = change.getCell(cc.field).getStringCellValue();
			// Check import requirements against command line
			if (change.getCell(cc.action).getStringCellValue() == "Modify") {
				if (((field == "attachments") && !cfg.exportAttachments)
						|| ((field == "Task") && !cfg.exportTasks)
						|| ((field == "comments") && !cfg.exportComments)) {
					d.p(Debug.WARN, "Ignoring action \"%s\" on item \"%s\", not set to import %s\n",
							change.getCell(cc.action).getStringCellValue(), item.getCell(titleCol).getStringCellValue(),
							field);
					continue; // Break out and try next change
				}
			}

			// If unset, it has a null value for the Leankit ID
			if ((item.getCell(idCol) == null) || (item.getCell(idCol).getStringCellValue() == "")) {
				// Check if this is a 'create' operation. If not, ignore and continue past.
				if (!change.getCell(cc.action).getStringCellValue().equals("Create")
						&& !(change.getCell(cc.action).getStringCellValue().equals("Modify")
								&& field.equals("Task"))
						&& !cfg.replay) {
					d.p(Debug.WARN, "Ignoring action \"%s\" on item \"%s\" (no ID present in item row: %d)\n",
							change.getCell(cc.action).getStringCellValue(), item.getCell(titleCol).getStringCellValue(),
							item.getRowNum());
					continue; // Break out and try next change
				}
			} else {
				// Check if this is a 'create' operation. If it is, ignore and continue past.
				if (change.getCell(cc.action).getStringCellValue().equals("Create") && !cfg.replay) {
					d.p(Debug.WARN,
							"Ignoring action \"%s\" on item \"%s\" (attempting create on existing ID in item row: %d)\n",
							change.getCell(cc.action).getStringCellValue(), item.getCell(titleCol).getStringCellValue(),
							item.getRowNum());
					continue; // Break out and try next change
				}

			}
			String id = null;
			if (change.getCell(cc.action).getStringCellValue().equals("Create")) {
				id = doAction(change, item);
				if (item.getCell(idCol) == null) {

					item.createCell(idCol);
				}
				item.getCell(idCol).setCellValue(id);
				XSSFFormulaEvaluator.evaluateAllFormulaCells(cfg.wb);

				d.p(Debug.INFO, "Create card \"%s\" (changes row %s)\n", id, change.getRowNum());
			} else {
				id = doAction(change, item);
				d.p(Debug.INFO, "Mod: \"%s\" on card \"%s\" (changes row %s)\n",
						field, id, change.getRowNum());
			}

			if (id == null) {

				d.p(Debug.WARN, "%s",
						"Got null back from doAction(). Most likely card deleted, but ID still in spreadsheet!\n");
			} else {
				XlUtils.writeFile(cfg, cfg.xlsxfn, cfg.wb);
			}
		}
	}

	private String doAction(Row change, Row item) {

		ChangesColumns cc = XlUtils.checkChangeSheetColumns(cfg.changesSheet);
		String cf = change.getCell(cc.row).getCellFormula();
		CellReference ca = new CellReference(cf);
		XSSFSheet iSht = cfg.wb.getSheet(ca.getSheetName());

		/**
		 * We need to get the header row for this sheet and work out which columns the
		 * fields are in. It is possible that fields could be different between sheets,
		 * so we have to do this every 'change'
		 */

		Iterator<Row> iRow = iSht.iterator();
		Row iFirst = iRow.next();
		/**
		 * Now iterate across the cells finding out which fields need to be set
		 */
		JSONObject fieldLst = new JSONObject();
		Iterator<Cell> cItor = iFirst.iterator();
		Integer idCol = null;
		Integer srcIdCol = null;
		while (cItor.hasNext()) {
			Cell cl = cItor.next();
			String nm = cl.getStringCellValue();
			if (nm.equals(ColNames.ID)) {
				idCol = cl.getColumnIndex();
				continue;
			} else if (nm.equals(ColNames.SOURCE_ID)) {
				srcIdCol = cl.getColumnIndex();
				continue;
			}
			fieldLst.put(nm, cl.getColumnIndex());
		}

		// If we come in as a replay, the ID field may not be set if we are doing
		// board to same-board
		if (cfg.replay) {
			if (item.getCell(idCol) == null) {
				idCol = srcIdCol;
			}
		}

		if (change.getCell(cc.action).getStringCellValue().equalsIgnoreCase("Create")) {
			// Now 'translate' the spreadsheet name:col pairs to fieldName:value pairs

			JSONObject flds = XlUtils.jsonCardFromRow(cfg, cfg.destination, fieldLst, item, null);

			// We need to find the ID of the board that this is targetting for a card
			// creation
			Board dst = LkUtils.getBoardByTitle(cfg, cfg.destination);
			if (!fieldLst.has("boardId") &&(dst != null)) {
				flds.put("boardId", dst.id);
			} else {
				flds.put("boardId", XlUtils.getCell(item, fieldLst.getInt("boardId")));
			}
			Card card = LkUtils.createCard(cfg, cfg.destination, flds); // Change from human readable to API fields on
			// the way
			if (card == null) {
				d.p(Debug.ERROR, "Could not create card on board \"%s\" with details: \"%s\"\n", flds.get("boardId"),
						flds.toString());
				System.exit(-25);
			}
			return card.id;

		} else if (change.getCell(cc.action).getStringCellValue().equalsIgnoreCase("Modify")) {
			// Fetch the ID from the item and then fetch that card
			Card card = LkUtils.getCard(cfg, cfg.destination, item.getCell(idCol).getStringCellValue());
			Card newCard = null;

			if (card == null) {
				d.p(Debug.WARN, "Could not locate card \"%s\"\n", item.getCell(idCol).getStringCellValue());
			} else {
				// Don't need this when modifying an existing item.
				if (fieldLst.has("boardId")) {
					fieldLst.remove("boardId");
				}
				JSONObject fld = new JSONObject();
				JSONObject vals = new JSONObject();

				String field = change.getCell(cc.field).getStringCellValue();
				SupportedXlsxFields allFields = new SupportedXlsxFields();

				try {
					// If its part of the fields we don't want, then ignore
					(allFields.new ReadOnly()).getClass().getField(field);
				} catch (NoSuchFieldException e) {

					switch (field) {
						case "Task": {
							// Get row for the task
							String tcf = change.getCell(cc.value).getCellFormula();
							CellReference tca = new CellReference(tcf);
							XSSFSheet cSheet = cfg.wb.getSheet(tca.getSheetName());
							Row task = cSheet.getRow(tca.getRow());

							JSONObject jsonTask = XlUtils.jsonCardFromRow(cfg, cfg.destination, fieldLst, task, card.id);
							if (task.getCell(idCol) == null) {

								task.createCell(idCol);
							}
							task.getCell(idCol).setCellValue(LkUtils.addTask(cfg, cfg.destination, card.id, jsonTask).id);
							break;
						}
						case "assignedUsers": {
							/**
							 * We need to try and match the email address in the destination and fetch the
							 * userID
							 */
							String usersList = change.getCell(cc.value).getStringCellValue();
							if (usersList != null) {
								ArrayList<BoardUser> boardUsers = LkUtils.getUsers(cfg, cfg.destination); // Fetch the
																											// board
																											// users
								if (boardUsers != null) {

									if (usersList != null) {
										String[] users = usersList.split(",");
										ArrayList<String> usersToPut = new ArrayList<>();
										for (int i = 0; i < users.length; i++) {
											User realUser = LkUtils.getUser(cfg, cfg.destination, users[i]);

											// Check if they are a board user so we don't error.
											for (int j = 0; j < boardUsers.size(); j++) {
												if (realUser.id.equals(boardUsers.get(j).id)) {
													usersToPut.add(realUser.id);
												}
											}
										}
										fld.put("assignedUserIds", usersToPut.toArray());
									}
								}
							}
							break;
						}
						case "customIcon": {
							// Incoming customIcon value is a name. We need to translate to
							// an id
							Cell cstmcell = change.getCell(cc.value);
							String cstmval = null;
							switch (cstmcell.getCellType()) {
								case FORMULA: {
									String ccf = cstmcell.getCellFormula();
									CellReference cca = new CellReference(ccf);
									XSSFSheet cSheet = cfg.wb.getSheet(cca.getSheetName());
									Row target = cSheet.getRow(cca.getRow());
									cstmval = target.getCell(cca.getCol()).getStringCellValue();
									break;
								}
								case STRING: {
									cstmval = cstmcell.getStringCellValue();
									break;
								}
								default: {
									break;
								}
							}
							CustomIcon ci = LkUtils.getCustomIcon(cfg, cfg.destination, cstmval);
							if (ci != null) {
								vals.put("value", ci.id);
								fld.put("customIconId", vals);
							}
							break;
						}
						case "lane": {
							String[] bits = ((String) XlUtils.getCell(change, cc.value)).split(InternalConfig.SPLIT_WIP_REGEX_CHAR);
							Lane foundLane = LkUtils.getLaneFromBoardTitle(cfg, cfg.destination, cfg.destination.getBoardName(),
									bits[0]);
							if (foundLane != null) {
								vals.put("value", foundLane.id);
								if (bits.length > 1) {
									vals.put("value2", bits[1]);
								}
								fld.put("Lane", vals);
							}
							break;
						}

						case "Parent": {
							if (cfg.nameResolver) {
								// Get the parentID originally associated with this card
								String parentId = change.getCell(cc.value).getStringCellValue();
								// Find the row with that ID in it
								Row parentRow = XlUtils.firstRowByStringValue(iSht, "srcID", parentId);
								//Get the title for that parent
								String parentTitle = ((String) XlUtils.getCell(parentRow,
									XlUtils.firstColumnFromSheet(iSht, "title")));
								//Get the latest version of it regardless of the original
								Card crd = LkUtils.getCardByTitle(cfg, cfg.destination, parentTitle);
								if ((crd != null) && (crd.id != null)) {
									vals.put("value", crd.id);
									fld.put(field, vals);
								}
								break;
							}
							// else fall-through and do the usual
						}
						// Fall-through from case: "Parent"
						default: {
							// Check if this is a standard/custom field and redo the 'put'

							CustomField ctmf = LkUtils.getCustomField(cfg, cfg.destination, field);
							if (ctmf != null) {
								vals.put("value", field);
								vals.put("value2", XlUtils.getCell(change, cc.value));
								fld.put("CustomField", vals);
							} else {
								vals.put("value", XlUtils.getCell(change, cc.value));
								fld.put(field, vals);
							}

							break;
						}
					}
				}

				newCard = LkUtils.updateCard(cfg, cfg.destination, card.id, fld);
				if (newCard == null) {
					d.p(Debug.ERROR, "Could not modify card \"%s\" on board %s with details: %s", card.id,
							cfg.destination.getBoardName(), fld.toString());
					System.exit(-26);
				}
				return card.id;
			}
		}
		// Unknown option comes here
		return null;
	}

}
