package de.tum.`in`.tumcampusapp.auxiliary

/**
 * Contains different constants used by several classes. Allows a unified access.
 */
object Const {
    const val MENSA_FOR_FAVORITEDISH = "FavoriteDishCafeteriaID"
    const val ACCESS_TOKEN = "access_token"
    const val ACTION_EXTRA = "action"
    const val ADDRESS_COLUMN = "address"
    const val BACKGROUND_MODE = "background_mode"
    const val CAFETERIA_ID = "cafeteriasId"
    const val CAFETERIAS = "cafeterias"
    const val CALENDAR_WEEK_MODE = "calender_week_mode"
    const val COMPLETED = "completed"
    const val DATABASE_NAME = "database.db"
    const val DATE = "date"
    const val DE = "de"
    const val DOWNLOAD_ALL_FROM_EXTERNAL = "download_all_from_external"
    const val ERROR = "error"
    const val MESSAGE = "message"
    const val FORCE_DOWNLOAD = "force_download"
    const val HIDE_WIZARD_ON_STARTUP = "hide_wizard_on_startup"
    const val HOURS_COLUMN = "hours"
    const val ID_COLUMN = "_id"
    const val JSON_ADDRESS = "address"
    const val JSON_ID = "id"
    const val JSON_LINK = "link"
    const val JSON_NAME = "name"
    const val JSON_RESULTS = "results"
    const val LINK_COLUMN = "link"
    const val LRZ_ID = "lrz_id"
    const val NAME_COLUMN = "name"
    const val NEWS = "news"
    const val KINO = "kino"
    const val FACULTIES = "faculties"
    const val STUDY_ROOM_GROUP_ID = "study_room_group_id"
    const val STUDY_ROOMS = "study_rooms"
    const val ORG_ID = "orgId"
    const val ORG_NAME = "orgName"
    const val ORG_PARENT_ID = "orgParentId"
    const val REMARK_COLUMN = "remark"
    const val ROLE = "card_role"
    const val ROOM_COLUMN = "room"
    const val URL_COLUMN = "url"
    const val SILENCE_ON = "silence_on"
    const val SILENCE_SERVICE = "silent_mode"
    const val SILENCE_OLD_STATE = "silence_old_state"
    const val SILENT_MODE_SET_TO = "silent_mode_set_to"
    const val SYNC_CLICKED = "automaticWidgetSyncButtonClick"
    const val TITLE_EXTRA = "title"
    const val TRANSPORT_COLUMN = "transport"

    const val API_HOSTNAME = "tumcabe.in.tum.de"
    const val CURRENT_CHAT_ROOM = "current_chat_room"
    const val CHAT_ROOM_DISPLAY_NAME = "chat_room_display_name"

    const val PRIVATE_KEY_ACTIVE = "chat_member_private_key_active"
    const val PRIVATE_KEY = "chat_member_private_key"
    const val PUBLIC_KEY = "chat_member_public_key"
    const val PUBLIC_KEY_UPLOADED = "chat_member_public_key_uploaded"

    const val TUMO_PIDENT_NR = "pIdentNr"
    const val TUMO_STUDENT_ID = "tumoStudentNr"
    const val TUMO_EXTERNAL_ID = "tumoExternNr"
    const val TUMO_EMPLOYEE_ID = "tumoBediensteteNr"
    const val TUMO_DISABLED = "tumo_is_disabled"

    const val GCM_REG_ID = "gcm_registration_id"
    const val GCM_REG_ID_SENT_TO_SERVER = "gcm_registration_id_sent_to_server"
    const val GCM_REG_ID_LAST_TRANSMISSION = "gcm_registration_id_last_transmission"
    const val GCM_INSTANCE_ID = "gcm_instance_id"
    const val GCM_TOKEN_ID = "gcm_token_id"
    const val CHAT_TERMS_SHOWN = "chat_terms_shown"
    const val APP_LAUNCHES = "app_launches"
    const val INTERNAL_PREFS = "internal_prefs"
    const val SYNC_CALENDAR = "sync_calendar"
    const val JSON_LATITUDE = "latitude"
    const val JSON_LONGITUDE = "longitude"
    const val PREFERENCE_SCREEN = "preference_screen"
    const val P_TOKEN = "pToken"
    const val APP_VERSION = "app_version"
    const val JSON_NEWS = "news"
    const val JSON_DATE = "date"
    const val JSON_TITLE = "title"
    const val JSON_IMAGE = "image"
    const val JSON_CREATED = "created"
    const val JSON_SRC = "src"
    const val JSON_ICON = "icon"
    const val JSON_SOURCE = "source"
    const val JSON_YEAR = "year"
    const val JSON_RUNTIME = "runtime"
    const val JSON_GENRE = "genre"
    const val JSON_DIRECTOR = "director"
    const val JSON_ACTORS = "actors"
    const val JSON_RATING = "rating"
    const val JSON_DESCRIPTION = "description"
    const val JSON_COVER = "cover"
    const val JSON_TRAILER = "trailer"
    const val JSON_KINO = "kino"
    const val JSON_FACULTY = "faculty"
    const val JSON_FACULTY_NAME = "name"
    const val JSON_QUESTION_ID = "question"
    const val JSON_QUESTION_TEXT = "text"
    const val JSON_QUESTION_FACULTY = "faculty"

    const val EVERYTHING_SETUP = "everything_setup"
    const val SYNC_CALENDAR_IMPORT = "calendar_import"
    const val GROUP_CHAT_ENABLED = "group_chat_enabled"
    const val TOKEN_IS_SETUP = "token_is_setup"
    const val BUG_REPORTS = "bug_reports"
    const val DEFAULT_CAMPUS = "card_default_campus"
    const val AUTO_JOIN_NEW_ROOMS = "auto_join_new_rooms"
    const val CHAT_MEMBER = "chat_member"
    const val POSITION = "position"

    const val PREF_UNIQUE_ID = "PREF_UNIQUE_ID"
    const val CHATTERMS_URL = "https://tumcabe.in.tum.de/landing/chatterms/"

    const val URL_ROOM_FINDER_API = "/Api/roomfinder/room/"
    const val URL_DEFAULT_MAP_IMAGE = "https://$API_HOSTNAME${URL_ROOM_FINDER_API}defaultMap/"
    const val URL_MAP_IMAGE = "https://$API_HOSTNAME${URL_ROOM_FINDER_API}map/"
    const val ROOM_ID = "room_id"

    const val NOTIFICATION_CHANNEL_DEFAULT = "default"

    const val BACKGROUND_SERVICE_JOB_ID = 1000
    const val SEND_MESSAGE_SERVICE_JOB_ID = 1001
    const val SILENCE_SERVICE_JOB_ID = 1002
    const val SEND_WIFI_SERVICE_JOB_ID = 1003
    const val DOWNLOAD_SERVICE_JOB_ID = 1004
    const val FILL_CACHE_SERVICE_JOB_ID = 1005

    const val GEOFENCING_SERVICE_JOB_ID = 1006
    const val ADD_GEOFENCE_EXTRA = "AddGeofence"
    const val DISTANCE_IN_METER = 50 * 1000f
    const val MUNICH_GEOFENCE = "geofence_munich_id"

    const val EXTRA_FOREIGN_CONFIGURATION_EXISTS = "CONFIGURED_BY_OTHER_APP"

    const val TUM_ID_PATTERN = "^[a-z]{2}[0-9]{2}[a-z]{3}$"

    const val WIFI_SCANS_ALLOWED = "WIFI_SCANS_ALLOWED"
    const val WIFI_SCAN_MINIMUM_BATTERY_LEVEL = "WIFI_SCAN_MINIMUM_BATTERY_LEVEL"
  
    const val DATE_AND_TIME = "yyyy-MM-dd HH:mm:ss"
    const val DATE_ONLY = "yyyy-MM-dd"
}
