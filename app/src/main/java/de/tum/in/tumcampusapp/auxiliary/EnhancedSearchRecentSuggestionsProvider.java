package de.tum.in.tumcampusapp.auxiliary;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import de.tum.in.tumcampusapp.managers.AbstractManager;

/**
 * Slightly modified version of SearchRecentSuggestionsProvider taken from AOSP source code
 */
public abstract class EnhancedSearchRecentSuggestionsProvider extends ContentProvider {
    // client-provided configuration values
    private String mId;
    private String mAuthority;
    private int mMode;
    private boolean mTwoLineDisplay;

    // general database configuration and tables
    private SQLiteDatabase db;
    private static final String S_SUGGESTIONS = "suggestions";
    private static final String ORDER_BY = "date DESC";
    private static final String NULL_COLUMN = "query";

    /**
     * This mode bit configures the database to record recent queries.  <i>required</i>
     *
     * @see #setupSuggestions(String, String)
     */
    private static final int DATABASE_MODE_QUERIES = 1;
    /**
     * This mode bit configures the database to include a 2nd annotation line with each entry.
     * <i>optional</i>
     *
     * @see #setupSuggestions(String, String)
     */
    private static final int DATABASE_MODE_2LINES = 2;

    // Uri and query support
    private static final int URI_MATCH_SUGGEST = 1;

    private Uri mSuggestionsUri;
    private UriMatcher mUriMatcher;

    private String mSuggestSuggestionClause;
    private String[] mSuggestionProjection;

    /**
     * In order to use this class, you must extend it, and call this setup function from your
     * constructor.  In your application or activities, you must provide the same values when
     * you create the {@link android.provider.SearchRecentSuggestions} helper.
     *
     * @param id        String identifying the SuggestionProvider. Must not contain blanks or any
     *                  other special characters
     * @param authority This must match the authority that you've declared in your manifest.
     * @see #DATABASE_MODE_QUERIES
     * @see #DATABASE_MODE_2LINES
     */
    void setupSuggestions(String id, String authority) {
        if (TextUtils.isEmpty(authority)) {
            throw new IllegalArgumentException();
        }
        // unpack mode flags
        mTwoLineDisplay = 0 != (EnhancedSearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES & DATABASE_MODE_2LINES);

        // saved values
        mId = '_' + id;
        mAuthority = authority;
        mMode = EnhancedSearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES;

        // derived values
        mSuggestionsUri = Uri.parse("content://" + mAuthority + "/suggestions");
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(mAuthority, SearchManager.SUGGEST_URI_PATH_QUERY, URI_MATCH_SUGGEST);

        if (mTwoLineDisplay) {
            mSuggestSuggestionClause = "display1 LIKE ? OR display2 LIKE ?";

            mSuggestionProjection = new String[]{
                    "0 AS " + SearchManager.SUGGEST_COLUMN_FORMAT,
                    "'android.resource://system/"
                    + android.R.drawable.ic_menu_recent_history + "' AS "
                    + SearchManager.SUGGEST_COLUMN_ICON_1,
                    "display1 AS " + SearchManager.SUGGEST_COLUMN_TEXT_1,
                    "display2 AS " + SearchManager.SUGGEST_COLUMN_TEXT_2,
                    "query AS " + SearchManager.SUGGEST_COLUMN_QUERY,
                    "_id"
            };
        } else {
            mSuggestSuggestionClause = "display1 LIKE ?";

            mSuggestionProjection = new String[]{
                    "0 AS " + SearchManager.SUGGEST_COLUMN_FORMAT,
                    "'android.resource://system/"
                    + android.R.drawable.ic_menu_recent_history + "' AS "
                    + SearchManager.SUGGEST_COLUMN_ICON_1,
                    "display1 AS " + SearchManager.SUGGEST_COLUMN_TEXT_1,
                    "query AS " + SearchManager.SUGGEST_COLUMN_QUERY,
                    "_id"
            };
        }
    }

    /**
     * This method is provided for use by the ContentResolver.  Do not override, or directly
     * call from your own code.
     */
    @Override
    public int delete(Uri uri, String select, String[] selectionArgs) {
        final int length = uri.getPathSegments()
                              .size();
        if (length != 1) {
            throw new IllegalArgumentException("Unknown Uri");
        }

        final String base = uri.getPathSegments()
                               .get(0);
        int count;
        if (base.equals(S_SUGGESTIONS)) {
            String selection = select.replace(S_SUGGESTIONS, S_SUGGESTIONS + mId);
            count = db.delete(S_SUGGESTIONS + mId, selection, selectionArgs);
        } else {
            throw new IllegalArgumentException("Unknown Uri");
        }
        getContext().getContentResolver()
                    .notifyChange(uri, null);
        return count;
    }

    /**
     * This method is provided for use by the ContentResolver.  Do not override, or directly
     * call from your own code.
     */
    @Override
    public String getType(Uri uri) {
        if (mUriMatcher.match(uri) == URI_MATCH_SUGGEST) {
            return SearchManager.SUGGEST_MIME_TYPE;
        }
        int length = uri.getPathSegments()
                        .size();
        if (length >= 1) {
            String base = uri.getPathSegments()
                             .get(0);
            if (base.equals(S_SUGGESTIONS)) {
                if (length == 1) {
                    return "vnd.android.cursor.dir/suggestion";
                } else if (length == 2) {
                    return "vnd.android.cursor.item/suggestion";
                }
            }
        }
        throw new IllegalArgumentException("Unknown Uri");
    }

    /**
     * This method is provided for use by the ContentResolver.  Do not override, or directly
     * call from your own code.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int length = uri.getPathSegments()
                        .size();
        if (length < 1) {
            throw new IllegalArgumentException("Unknown Uri");
        }
        // Note:  This table has on-conflict-replace semantics, so insert() may actually replace()
        long rowID = -1;
        String base = uri.getPathSegments()
                         .get(0);
        Uri newUri = null;
        if (base.equals(S_SUGGESTIONS) && length == 1) {
            rowID = db.insert(S_SUGGESTIONS + mId, NULL_COLUMN, values);
            if (rowID > 0) {
                newUri = Uri.withAppendedPath(mSuggestionsUri, String.valueOf(rowID));
            }
        }
        if (rowID < 0) {
            throw new IllegalArgumentException("Unknown Uri");
        }
        getContext().getContentResolver()
                    .notifyChange(newUri, null);
        return newUri;
    }

    /**
     * This method is provided for use by the ContentResolver.  Do not override, or directly
     * call from your own code.
     */
    @Override
    public boolean onCreate() {
        if (mAuthority == null || mMode == 0) {
            throw new IllegalArgumentException("Provider not configured");
        }
        db = AbstractManager.getDb(getContext());
        final String create_table = "CREATE TABLE IF NOT EXISTS suggestions" + mId + " (" +
                                    "_id INTEGER PRIMARY KEY" +
                                    ",display1 TEXT UNIQUE ON CONFLICT REPLACE" +
                                    ",query TEXT" +
                                    ",date LONG );";
        db.execSQL(create_table);
        return true;
    }

    /**
     * This method is provided for use by the ContentResolver.  Do not override, or directly
     * call from your own code.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // special case for actual suggestions (from search manager)
        if (mUriMatcher.match(uri) == URI_MATCH_SUGGEST) {
            String suggestSelection;
            String[] myArgs;
            if (TextUtils.isEmpty(selectionArgs[0])) {
                suggestSelection = null;
                myArgs = null;
            } else {
                String like = "%" + selectionArgs[0] + "%";
                if (mTwoLineDisplay) {
                    myArgs = new String[]{like, like};
                } else {
                    myArgs = new String[]{like};
                }
                suggestSelection = mSuggestSuggestionClause;
            }
            // Suggestions are always performed with the default sort order
            Cursor c = db.query(S_SUGGESTIONS + mId, mSuggestionProjection,
                                suggestSelection, myArgs, null, null, ORDER_BY, null);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        }

        // otherwise process arguments and perform a standard query
        int length = uri.getPathSegments()
                        .size();
        if (length != 1 && length != 2) {
            throw new IllegalArgumentException("Unknown Uri");
        }

        String base = uri.getPathSegments()
                         .get(0);
        if (!base.equals(S_SUGGESTIONS)) {
            throw new IllegalArgumentException("Unknown Uri");
        }

        String[] useProjection = null;
        if (projection != null && projection.length > 0) {
            useProjection = new String[projection.length + 1];
            System.arraycopy(projection, 0, useProjection, 0, projection.length);
            useProjection[projection.length] = "_id AS _id";
        }

        StringBuilder whereClause = new StringBuilder(256);
        if (length == 2) {
            whereClause.append("(_id = ")
                       .append(uri.getPathSegments()
                                  .get(1))
                       .append(')');
        }

        // Tack on the user's selection, if present
        if (selection != null && !selection.isEmpty()) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }

            whereClause.append('(');
            whereClause.append(selection);
            whereClause.append(')');
        }

        // And perform the generic query as requested
        Cursor c = db.query(base + mId, useProjection, whereClause.toString(),
                            selectionArgs, null, null, sortOrder,
                            null);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    /**
     * This method is provided for use by the ContentResolver.  Do not override, or directly
     * call from your own code.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not implemented");
    }

}