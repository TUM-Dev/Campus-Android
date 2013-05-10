package de.tum.in.tumcampusapp.auxiliary;

/** defines constants for database and settings */
public final class Const {

	/** constants for application settings */
	public static final class Settings {
		public static final String APP_DETAILS_SETTINGS_KEY = "app_details";

		/** filter cafeterias by a substring */
		public final static String cafeteriaFilter = "cafeteriaFilter";

		/** activate debug mode (debug activity and detailed error handling) */
		public final static String debug = "debug";

		public static final String MARKET_SETTINGS_KEY = "market";

		/** enable silence service, silence the mobile during lectures */
		public final static String silence = "silence";
		/**
		 * mobile is switched to silence added manually by Florian Schulz
		 */
		public final static String silence_on = "silence_on";
		/** Settings keys */

		public static final String TUMONLINE_SETTINGS_KEY = "tumonline";
	}

	/** Identifier of access token */
	public static final String ACCESS_TOKEN = "access_token";

	/** Action identifier */
	public static final String ACTION_EXTRA = "action";

	/** Identifier of the Address column */
	public static final String ADDRESS_COLUMN = "address";

	/** Action value "cafeterias" */
	public static final String CAFETERIAS = "cafeterias";

	/** Action value "completed" */
	public static final String COMPLETED = "completed";

	/** Key for the curricula file */
	public static final String CURRICULA = "curricula";

	/** Identifier of the date column */
	public static final String DATE_COLUMN_DE = "date_de";

	/** database filename */
	public final static String db = "database.db";

	/** Action values and filenames *************************************** */

	/** database version used by SQLiteOpenHelper */
	public final static int dbVersion = 1;

	/** Identifier for the German language */
	public static final String DE = "de";

	/** Action value "defaults" */
	public static final String DEFAULTS = "defaults";

	/** Identifier of the Description column */
	public static final String DESCRIPTION_COLUMN = "description";

	/** Action value "documents" */
	public static final String DOCUMENTS = "documents";

	/** Identifier for the English language */
	public static final String EN = "en";

	/** Identifier of the End column */
	public static final String END_DE_COLUMN = "end_de";

	/** Lecture ending date */
	public static final String END_DT_COLUMN = "end_dt";

	/** Error (used in extras) */
	public static final String ERROR = "error";

	/** Action value "events" */
	public static final String EVENTS = "events";

	public static final String FACEBOOK = "www.facebook.com";

	/** Action value "feeds" */
	public static final String FEEDS = "feeds";

	/** Action value "gallery" */
	public static final String GALLERY = "gallery";

	/** Holiday */
	public static final String HOLIDAY = "holiday";

	/** Identifier of the Hours column */
	public static final String HOURS_COLUMN = "hours";

	/** Identifier of the id column */
	public static final String ID_COLUMN = "_id";

	/** ID identifier */
	public static final String ID_EXTRA = "id";

	/** Identifier of the Image column */
	public static final String IMAGE_COLUMN = "image";

	/** Identifier of the LectureId column */
	public static final String LECTURE_ID_COLUMN = "lectureId";

	/** Action value "lectures" */
	public static final String LECTURES = "lectures";

	/** Action value "lecturesTUMOnline" */
	public static final String LECTURES_TUM_ONLINE = "lecturesTUMOnline";

	/** Identifier of the Link column */
	public static final String LINK_COLUMN = "link";

	/** Action value "links" */
	public static final String LINKS = "links";

	/** Identifier of the Location column */
	public static final String LOCATION_COLUMN = "location";

	// TODO Check whether there it makes sense to export to strings (because of
	// identifier for SharedPreferences)
	/** LRZ_ID identifier */
	public static final String LRZ_ID = "lrz_id";

	/** Message identifier */
	public static final String MESSAGE_EXTRA = "message";

	/** Identifier of the module column */
	public static final String MODULE_COLUMN = "module";

	/** Identifier of the name column */
	public static final String NAME_COLUMN = "name";

	/** Action value "news" */
	public static final String NEWS = "news";

	/** Identifier of the Note ("Notiz" not Grade) column */
	public static final String NOTE_COLUMN = "note";

	/** Action value "noten" */
	public static final String NOTEN = "noten";

	/** Extra value "orgId" */
	public static final String ORG_ID = "orgId";

	/** Extra value "ORG_NAME" */
	public static final String ORG_NAME = "orgName";

	/** Extra value "orgParentId" */
	public static final String ORG_PARENT_ID = "orgParentId";

	/** Action value and filename "organisations" */
	public static final String ORGANISATIONS = "organisations";

	/** Identifier of the Remark column */
	public static final String REMARK_COLUMN = "remark";

	/** Identifier of the Room column */
	public static final String ROOM_COLUMN = "room";

	/** Action value and filename "roomfinder" */
	public static final String ROOMFINDER = "roomfinder";

	/** Identifier of the Start column */
	public static final String START_DE_COLUMN = "start_de";

	// TODO IMPORTANT Check whether "start_dt" and "start_de" are actually the
	// same
	/** Lecture starting date */
	public static final String START_DT_COLUMN = "start_dt";

	/** Action value "studienbeitragsstatus" */
	public static final String STUDIENBEITRAGSTATUS = "studienbeitragsstatus";

	/** Title identifier */
	public static final String TITLE_EXTRA = "title";

	/** Identifier of the Transport column */
	public static final String TRANSPORT_COLUMN = "transport";

	/** TUMONLINE_PASSWORD identifier */
	public static final String TUMONLINE_PASSWORD = "tumonline_password";

	public static final String URL = "url";

	/** Identifier of the URL column */
	public static final String URL_COLUMN = "url";

	/** Vacation */
	public static final String VACATION = "vacation";

	/** Identifier of the Weekday column */
	public static final String WEEKDAY_COLUMN = "weekday";
}