package com.planview.lkutility.System;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Need to set chcp 1252 on Windows CMD to get correct characters
 * 
 * Excuse the Google Translate usage.... :-)
 * 
 */

public class FrenchLang {

	private HashMap<Integer, String> map;

	public FrenchLang() {
		try {
			map = new HashMap<Integer, String>(Map.ofEntries(
					// Main message beginnings
					new AbstractMap.SimpleEntry<Integer, String>(LMS.INFO,
							"Info: "),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.VERBOSE,
							"Verbeaux: "),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.WARN,
							"Avertissement: "),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ERROR,
							"Erreur: "),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ALWAYS,
							"Avis: "),

					// Command line options
					new AbstractMap.SimpleEntry<Integer, String>(LMS.FILE_OPTION,
							"filename"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.FILE_OPTION_MSG,
							"XLSX Spreadsheet (must contain API config!)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION,
							"supprimer"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.REMOVE_OPTION_MSG,
							"Supprimer les tableaux cibles (delete)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_OPTION,
							"repeter"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_OPTION_MSG,
							"Exécuter automatiquement la réinitialisation de la destination pendant le diff"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION, "refaire"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.REMAKE_OPTION_MSG,
							"Refaire les tableaux cibles en archivant les anciens et en ajoutant de nouveaux"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.COMPARE_OPTION,
							"comparer"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.COMPARE_OPTION_MSG,
							"Comparer l'URL de destination à un transfert précédent"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.IMPORT_OPTION,
							"importer"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.IMPORT_OPTION_MSG,
							"Exécuter le programme d'importation"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.EXPORT_OPTION,
							"exporter"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.EXPORT_OPTION_MSG,
							"Exécuter le programme d'exportation"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_X_OPTION,
							"xlsx"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_X_OPTION_MSG,
							"Supprimer des fiches sur les tableaux cibles (à partir d'une feuille de calcul)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.LANGUAGE_OPTION,
							"langue"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.LANGUAGE_OPTION_MSG,
							"Langue, language, sprache (fr, en, de)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_OPTION,
							"delete"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DELETE_OPTION_MSG,
							"Supprimer toutes les cartes sur les tableaux cibles"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.TASKTOP_OPTION,
							"tasktop"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.TASKTOP_OPTION_MSG,
							"Suivez les liens externes pour supprimer les artefacts distants"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.GROUP_OPTION,
							"group"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.GROUP_OPTION_MSG,
							"Identifiant du groupe à traiter (si présent)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.MOVE_OPTION,
							"move"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.MOVE_OPTION_MSG,
							"Piste pour modifier les cartes indésirables avec (pour comparaison uniquement)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DEBUG_OPTION,
							"debug"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DEBUG_OPTION_MSG,
							"Imprimez des tas de choses utiles : 0 - Erreur, 1 - Et Avertissements, 2 - Et Infos, 3 - Et Débogage, 4 - Et Réseau"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ARCHIVED_OPTION,
							"archived"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ARCHIVED_OPTION_MSG,
							"Inclure les anciennes cartes archivées dans l'exportation (le cas échéant)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.TASKS_OPTION,
							"tasks"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.TASKS_OPTION_MSG,
							"Inclure les fiches de tâches dans l'exportation (le cas échéant)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ATTACHMENTS_OPTION,
							"attachments"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ATTACHMENTS_OPTION_MSG,
							"Exporter les pièces jointes de la carte dans le système de fichiers local (le cas échéant)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMENTS_OPTION,
							"comments"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMENTS_OPTION_MSG,
							"Exporter les commentaires de la carte dans le système de fichiers local (si présent)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ORIGIN_OPTION,
							"origin"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.ORIGIN_OPTION_MSG,
							"Ajouter un commentaire pour l'enregistrement de l'artefact source"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.RO_OPTION,
							"ro"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.RO_OPTION_MSG,
							"Exporter les champs en lecture seule (non importés!)"),

					// Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.FINISH_PROGRAM,
							"Terminé à : %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.START_PROGRAM,
							"Commencé à: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SETTING_LANGUAGE,
							"Mettre la langue en: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.COMMANDLINE_ERROR,
							"D'option de ligne de commande: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SHEET_NOTFOUND_ERROR,
							"La feuille requise n'a pas été détectée dans la feuille de calcul: \"Config\""),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SHT_HDR_ERROR,
							"N'a détecté aucune information d'en-tête sur la feuille de configuration (première ligne!)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SHT_COL_ERROR,
							"N'a pas détecté les colonnes correctes sur la feuille de configuration"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SHT_ITR_ERROR,
							"N'a détecté aucune information de transfert potentiel sur la feuille de configuration (la première cellule doit être non vide, par exemple l'URL vers un hôte réel)"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.REPLAY_SHEET_NOT_FOUND,
							"Feuille de relecture introuvable. Exécuter avec -c avant (ou avec) -a\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_COMPARE_MODE,
							"Mise en mode \"comparer\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.INVALID_OPTIONS,
							"Options non valides spécifiées (-r avec un autre). Par défaut en mode \"repeter\".\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SET_REPLAY_MODE,
							"Passe en mode \"repeter\"\n"),

					// LeanKitAccess.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.NO_HTTP,
							"L'accès http à AgilePlace n'est pas pris en charge. Passage en https\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.STATUSCODE_ERROR,
							"\"%s\" a donné la réponse \"%s\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.UNKNOWN_API_TYPE_ERROR,
							"Type d'élément non pris en charge demandé à partir de l'API du serveur: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.RECEIVED_DATA,
							"Reçu %d %s (sur %d)\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.UNRECOGNISED_TYPE,
							"Oops! ne reconnaît pas le type d'élément demandé:"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.CANNOT_ENCODE_BOARD,
							"Impossible d'encoder le nom de la carte"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DOWNLOAD_ATT_TYPE,
							"Le type de pièce jointe téléchargée est: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.AUTO_FROM_SCRIPT,
							"Généré automatiquement à partir du script"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.SETTING_PARENT,
							"Tentative de définition du parent de %s sur la valeur \"%s\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.EXTLINK_ERROR,
							"Impossible d'extraire le lien externe de %s (possible ',' dans l'étiquette ?)"),

					// NetworkAccess.java output messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.NEED_SECURE_MODE,
							"Accès http non pris en charge. Basculement vers https"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.APIKEY_ERROR,
							"Aucune clé API valide fournie"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.CREDS_ERROR,
							"Non autorisé. Vérifier les informations d'identification dans la feuille de calcul:"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.NETWORK_FAULT_ERROR,
							"Défaut réseau"),

					// BoardCreator.java output messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.BOARD_COPY_ERROR,
							"Impossible de dupliquer localement de \"%s\" à \"%s\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.BOARD_NOT_FOUND_ERROR,
							"Impossible de localiser le tableau avec le titre: \"%s\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.BOARD_CREATE_ERROR,
							"Impossible de créer/localiser le tableau de destination"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.LAYOUT_CONV_ERROR,
							"La conversion de la mise en page a échoué: "),

					// Importer.java output messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.IMP_NO_CHG_SHT,
							"Impossible de trouver la feuille de modifications requise dans le fichier: %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.IMP_CREATE_FAIL,
							"Impossible de créer la carte à bord \"%s\" avec les détails: \"%s\"\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.IMP_MOD_FAIL,
							"Impossible de modifier la carte \"%s\" sur la tableau %s avec les détails: %s"),

					// Diff.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DIFF_NOT_FOUND,
							"Feuilles introuvables pour la tableau src:"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.DIFF_FETCH_ERROR,
							"Oops! récupération de nouvelles données pour la tableau: %s a échoué\n"),

					// Exporter.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.EXP_INVALID_TYPE,
							"Type de carte non valide - vérifiez le paramètre \"Tâche\" sur \"%s\". Choisir d'utiliser la voie \"%s\"\n"),

					// XlUtils.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.XLUTILS_COLS_ERROR,
							"Impossible de trouver toutes les colonnes requises dans la feuille"),

					// AzureDeleter.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.AZURE_DELETE,
							"Supprimé %s\n"),
					new AbstractMap.SimpleEntry<Integer, String>(LMS.AZURE_DELETE_FAIL,
							"Échec de la suppression %s\n"),

					// JiraDeleter.java Output Messages
					new AbstractMap.SimpleEntry<Integer, String>(LMS.JIRA_DELETE,
							"Tentative de suppression %s\n")

			));
		} catch (Exception e) {
			System.out.printf("French Language Map Error: %s\n", e.getMessage());
		}
	}

	public HashMap<Integer, String> getMap() {
		return map;
	}
}
