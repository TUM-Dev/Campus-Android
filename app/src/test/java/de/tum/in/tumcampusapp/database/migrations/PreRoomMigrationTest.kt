package de.tum.`in`.tumcampusapp.database.migrations

import android.arch.persistence.room.util.TableInfo
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.TestApp
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApp::class)
class PreRoomMigrationTest {
    private val preRoomSchema =
            """CREATE TABLE faculties (faculty INTEGER, name VARCHAR);
               CREATE TABLE openQuestions (question INTEGER PRIMARY KEY, text VARCHAR, created VARCHAR, end VARCHAR, answerid INTEGER, answered BOOLEAN, synced BOOLEAN);
               CREATE TABLE ownQuestions (question INTEGER PRIMARY KEY, text VARCHAR, targetFac VARCHAR, created VARCHAR, end VARCHAR, yes INTEGER, no INTEGER, deleted BOOLEAN, synced BOOLEAN);
               CREATE TABLE tumLocks (url VARCHAR UNIQUE, error VARCHAR, timestamp VARCHAR, lockedFor INT, active INT);
               CREATE TABLE chat_room (room INTEGER, name VARCHAR, semester VARCHAR, semester_id VARCHAR, joined INTEGER, _id INTEGER, contributor VARCHAR, members INTEGER, PRIMARY KEY(name, semester_id));
               CREATE TABLE chat_message (_id INTEGER PRIMARY KEY, previous INTEGER, room INTEGER, text TEXT, timestamp VARCHAR, signature TEXT, member BLOB, read INTEGER, sending INTEGER);
               CREATE TABLE syncs (id VARCHAR PRIMARY KEY, lastSync VARCHAR);
               CREATE TABLE unsent_chat_message (_id INTEGER PRIMARY KEY AUTOINCREMENT, room INTEGER, text TEXT, member BLOB, msg_id INTEGER);
               CREATE TABLE cafeterias (id INTEGER PRIMARY KEY, name VARCHAR, address VARCHAR, latitude REAL, longitude REAL);
               CREATE TABLE cafeterias_menus (id INTEGER PRIMARY KEY AUTOINCREMENT, mensaId INTEGER KEY, date VARCHAR, typeShort VARCHAR, typeLong VARCHAR, typeNr INTEGER, name VARCHAR);
               CREATE TABLE favorite_dishes (id INTEGER PRIMARY KEY AUTOINCREMENT, mensaId INTEGER, dishName VARCHAR,date VARCHAR, tag VARCHAR);
               CREATE TABLE kino (id INTEGER PRIMARY KEY, title TEXT, year VARCHAR, runtime VARCHAR,genre VARCHAR, director TEXT, actors TEXT, rating VARCHAR, description TEXT, cover TEXT, trailer TEXT, date VARCHAR, created VARCHAR,link TEXT);
               CREATE TABLE news_sources (id INTEGER PRIMARY KEY, icon VARCHAR, title VARCHAR);
               CREATE TABLE news (id INTEGER PRIMARY KEY, src INTEGER, title TEXT, link VARCHAR, image VARCHAR, date VARCHAR, created VARCHAR, dismissed INTEGER);
               CREATE TABLE room_locations (title VARCHAR PRIMARY KEY, latitude VARCHAR, longitude VARCHAR);
               CREATE TABLE calendar (nr VARCHAR PRIMARY KEY, status VARCHAR, url VARCHAR, title VARCHAR, description VARCHAR, dtstart VARCHAR, dtend VARCHAR, location VARCHAR REFERENCES room_locations);
               CREATE TABLE widgets_timetable_blacklist (widget_id INTEGER, lecture_title VARCHAR, PRIMARY KEY (widget_id, lecture_title));
               CREATE TABLE buildings2gps (id VARCHAR PRIMARY KEY, latitude VARCHAR, longitude VARCHAR);
               CREATE TABLE locations (id INTEGER PRIMARY KEY, category VARCHAR, name VARCHAR, address VARCHAR, room VARCHAR, transport VARCHAR, hours VARCHAR, remark VARCHAR, url VARCHAR);
               CREATE TABLE transport_favorites (id INTEGER PRIMARY KEY AUTOINCREMENT, symbol VARCHAR);
               CREATE TABLE widgets_transport (id INTEGER PRIMARY KEY, station VARCHAR, station_id VARCHAR, location BOOLEAN, reload BOOLEAN);
               CREATE TABLE suggestions_rooms (_id INTEGER PRIMARY KEY,display1 TEXT UNIQUE ON CONFLICT REPLACE,query TEXT,date LONG );
               CREATE TABLE suggestions_mvv (_id INTEGER PRIMARY KEY,display1 TEXT UNIQUE ON CONFLICT REPLACE,query TEXT,date LONG );
               CREATE TABLE suggestions_persons (_id INTEGER PRIMARY KEY,display1 TEXT UNIQUE ON CONFLICT REPLACE,query TEXT,date LONG );
               CREATE TABLE suggestions_lecture (_id INTEGER PRIMARY KEY,display1 TEXT UNIQUE ON CONFLICT REPLACE,query TEXT,date LONG );
               CREATE TABLE recents (typ INTEGER, name VARCHAR UNIQUE);
               CREATE TABLE study_room_groups (id INTEGER PRIMARY KEY, name VARCHAR, details VARCHAR);
               CREATE TABLE study_rooms (id INTEGER PRIMARY KEY, code VARCHAR, name VARCHAR, location VARCHAR, occupied_till VARCHAR, group_id INTEGER);"""

    private lateinit var db: SQLiteDatabase

    @Before
    fun setUp() {
        val openHelper = object : SQLiteOpenHelper(RuntimeEnvironment.application, Const.DATABASE_NAME, null, 1) {
            override fun onCreate(db: SQLiteDatabase?) {
                preRoomSchema.splitToSequence('\n').forEach {
                    db!!.execSQL(it)
                }
            }

            override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) = Unit
        }

        db = openHelper.writableDatabase
    }

    @Test
    fun simpleTcaDbMigration() {
        db.close()
        /** @see TableInfo.Column.primaryKeyPosition
         *  Room checks primaryKeyPosition on SDK >= 20 and notes: "custom SQLite deployments may return false positives"
         *  Roboelectric is such a custom deployment, so work around that
         */
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 19)
        val tcadb = TcaDb.getInstance(RuntimeEnvironment.application)
        assert(tcadb.newsSourcesDao().getNewsSources("test").isEmpty())
    }

    @After
    fun tearDown() {
        db.close()
        TcaDb.getInstance(RuntimeEnvironment.application).close()
        File(db.path).delete()
    }
}