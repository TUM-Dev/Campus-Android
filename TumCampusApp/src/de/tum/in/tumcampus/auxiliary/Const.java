package de.tum.in.tumcampus.auxiliary;

/**
 * Contains different constants used by several classes. Allows a unified
 * access.
 * 
 * @author Sascha Moecker
 * 
 */
public final class Const {

	public static final String ACCESS_TOKEN = "access_token";
	public static final String ACTION_EXTRA = "action";
	public static final String ADDRESS_COLUMN = "address";
	public static final String BACKGROUND_MODE = "background_mode";
	public static final String CAFETERIA_ID = "cafeteriasId";
	public static final String CAFETERIA_NAME = "cafeteriasName";
	public static final String CAFETERIAS = "cafeterias";
	public static final String CALENDAR_URI = "calendar_uri";
	public static final String CALENDER = "kalender";
	public static final String COLOR_SCHEME = "color_scheme";
	public static final String COMPLETED = "completed";
	public static final String CURRICULA = "curricula";
	public final static String DATABASE_NAME = "database.db";
	public static final String DATE = "date";
	public static final String DATE_COLUMN_DE = "date_de";
	public static final String DE = "de";
	public static final String DEFAULTS = "defaults";
	public static final String DESCRIPTION_COLUMN = "description";
	public static final String DOCUMENTS = "documents";
	public static final String DOWNLOAD_ALL_FROM_EXTERNAL = "download_all_from_external";
	public static final String EN = "en";
	public static final String END_DE_COLUMN = "end_de";
	public static final String END_DT_COLUMN = "end_dt";
	public static final String ERROR = "error";
	public static final String ERROR_MESSAGE = "error_message";
	public static final String EVENTS = "events";
	public static final String FACEBOOK_URL = "www.facebook.com";
	public static final String FEED_ID = "feedId";
	public static final String FEED_NAME = "feedName";
	public static final String FEEDS = "feeds";
	public static final String FETCH_NOTHING = "";
	public static final String FIRST_RUN = "first_run";
	public static final String FORCE_DOWNLOAD = "force_download";
	public static final String GALLERY = "gallery";
	public static final String[] GRADES = { "1,0", "1,3", "1,4", "1,7", "2,0",
			"2,3", "2,4", "2,7", "3,0", "3,3", "3,4", "3,7", "4,0", "4,3",
			"4,4", "4,7", "5,0" };
	public static final String HIDE_WIZZARD_ON_STARTUP = "hide_wizzard_on_startup";
	public static final String HOLIDAY = "holiday";
	public static final String HOURS_COLUMN = "hours";
	public static final int HTTP_TIMEOUT = 25000;
	public static final String ID_COLUMN = "_id";
	public static final String ID_EXTRA = "id";
	public static final String IMAGE_COLUMN = "image";
	public static final String JSON_ANSCHRIFT = "anschrift";
	public static final String JSON_CAPTION = "caption";
	public static final String JSON_CREATED_TIME = "created_time";
	public static final String JSON_DATA = "data";
	public static final String JSON_DESCRIPTION = "description";
	public static final String JSON_ENCLOSURE = "enclosure";
	public static final String JSON_END_TIME = "end_time";
	public static final String JSON_FROM = "from";
	public static final String JSON_ID = "id";
	public static final String JSON_LINK = "link";
	public static final String JSON_LOCATION = "location";
	public static final String JSON_MENSA_MENSEN = "mensa_mensen";
	public static final String JSON_MESSAGE = "message";
	public static final String JSON_NAME = "name";
	public static final String JSON_OBJECT_ID = "object_id";
	public static final String JSON_PICTURE = "picture";
	public static final String JSON_PUB_DATE = "pubDate";
	public static final String JSON_RESULTS = "results";
	public static final String JSON_START_TIME = "start_time";
	public static final String JSON_TITLE = "title";
	public static final String LECTURE_ID_COLUMN = "lectureId";
	public static final String LECTURES = "lectures";
	public static final String LECTURES_APPOINTMENTS = "veranstaltungenTermine";
	public static final String LECTURES_DETAILS = "veranstaltungenDetails";
	public static final String LECTURES_PERSONAL = "veranstaltungenEigene";
	public static final String LECTURES_SEARCH = "veranstaltungenSuche";
	public static final String LECTURES_TUM_ONLINE = "lecturesTUMOnline";
	public static final String LECTURES_TUM_ONLINE_FINISH = "lecturesTUMOnlineFinish";
	public static final String LECTURES_TUM_ONLINE_START = "lecturesTUMOnlineStart";
	public static final String LINK_COLUMN = "link";
	public static final String LINKS = "links";
	public static final String LOCATION_COLUMN = "location";
	public static final String LRZ_ID = "lrz_id";
	public static final String MESSAGE_EXTRA = "message";
	public static final String MODULE_COLUMN = "module";
	public static final String NAME_COLUMN = "name";
	public static final String NEWS = "news";
	public static final String NOTE_COLUMN = "note";
	public static final String NOTEN = "noten";
	public static final String ORG_DETAILS = "orgDetails";
	public static final String ORG_ID = "orgId";
	public static final String ORG_NAME = "orgName";
	public static final String ORG_PARENT_ID = "orgParentId";
	public static final String ORG_TREE = "orgBaum";
	public static final String ORGANISATIONS = "organisations";
	public static final String PREFS_HAVE_CHANGED = "prefs_have_changed";
	public static final String REMARK_COLUMN = "remark";
	public static final String ROLE = "role";
	public static final String ROOM_COLUMN = "room";
	public static final String ROOMFINDER = "roomfinder";
	public static final String SILENCE_ON = "silence_on";
	public static final String SILENCE_SERVICE = "silent_mode";
	public static final String START_DE_COLUMN = "start_de";
	public static final String START_DT_COLUMN = "start_dt";
	public static final String STUDIENBEITRAGSTATUS = "studienbeitragsstatus";
	public static final String TITLE_EXTRA = "title";
	public static final String TRANSPORT_COLUMN = "transport";
	public static final String TUMONLINE_PASSWORD = "tumonline_password";
	public static final String URL = "url";
	public static final String URL_COLUMN = "url";
	public static final String VACATION = "vacation";
	public static final String WARNING = "warning";
	public static final String WARNING_MESSAGE = "warning_message";
	public static final String WEEKDAY_COLUMN = "weekday";
	public static final String Grade_Count = "GradeCount";
	public static final String MVV_ID="mvv_id";
	public static final String LECTURE_ID="lectures_id";
	public static final String MENUES_ID="menues_id";
	public static final String GRADES_ID="grades_id";
	public static final String RSS_FEEDS_ID="rss_feeds_id";
	public static final String CALENDER_ID="calender_id";
	public static final String STUDY_PLANS_ID="study_plans_id";
	public static final String EVENTS_ID="events_id";
	public static final String GALLERY_ID="gallery_id";
	public static final String PERSON_SEARCH_ID="person_search_id";
	public static final String PLANS_ID="plans_id";
	public static final String ROOMFINDER_ID="roomfinder_id";
	public static final String OPENING_HOURS_ID="opening_hours_id";
	public static final String ORGANISATIONS_ID="organisations_id";
	public static final String MY_LECTURES_ID="my_lectures_id";
	public static final String MY_GRADES_ID="my_grades_id";
	public static final String TUITION_FEES_ID="tuition_fees_id";
	public static final String TUM_NEWS_ID="tum_news_id";
	public static final String INFORMATION_ID="information_id";
	
	public static final String CHAT_ROOMS_ID="chat_rooms_id";
	public static final String CURRENT_CHAT_ROOM = "current_chat_room";
	public static final String CURRENT_CHAT_MEMBER = "current_chat_member";
	public static final String CHAT_ROOM_DISPLAY_NAME = "chat_room_display_name";
	public static final String PRIVATE_KEY = "chat_member_private_key";
	public static final String PUBLIC_KEY = "chat_member_public_key";
	public static final String GCM_REG_ID = "gcm_registration_id";
	public static final String GCM_REG_ID_SENT_TO_SERVER = "gcm_registration_id_sent_to_server";
	public static final String CHAT_TERMS_SHOWN = "chat_terms_shown";
    
}