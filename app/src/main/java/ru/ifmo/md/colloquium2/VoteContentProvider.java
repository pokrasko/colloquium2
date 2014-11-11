package ru.ifmo.md.colloquium2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by pokrasko on 11.11.14.
 */
public class VoteContentProvider extends ContentProvider {
    private static final int PERSONS = 0;
    private static final int PERSON_ID = 1;

    public static final String ID_FIELD = "id";
    public static final String NAME_FIELD = "name";
    public static final String VOTES_FIELD = "votes";

    public static final int RENAME_ACTION = 0;
    public static final int VOTE_ACTION = 1;

    private static final String AUTHORITY = "ru.ifmo.md.colloquium2";
    private static final String PERSONS_PATH = "persons";
    public static final Uri CONTENT_PERSONS_URI = Uri.parse("content://" +
        AUTHORITY + "/" + PERSONS_PATH);

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        matcher.addURI(AUTHORITY, PERSONS_PATH, PERSONS);
        matcher.addURI(AUTHORITY, PERSONS_PATH + "/#", PERSON_ID);
    }

    static String DB_NAME = "persons.db";
    static int DB_VERSION = 1;
    private static final String PERSONS_TABLE = "persons";
    private PersonDBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new PersonDBHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        int uriType = matcher.match(uri);
        switch (uriType) {
            case PERSONS:
                id = db.insert(PERSONS_TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Bad URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(uri, "" + id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int uriType = matcher.match(uri);

        int updatedRows = 0;
        switch (uriType) {
            case PERSON_ID:
                int action = values.getAsInteger("action");
                String id = uri.getLastPathSegment();
                if (action == VOTE_ACTION) {
                    String[] projection = {"votes"};
                    Cursor person = db.query(PERSONS_TABLE, projection, ID_FIELD + "=" + id, null, null, null, null);
                    long votes = person.getInt(2);
                    votes++;
                    values.put(VOTES_FIELD, votes);
                }
                updatedRows = db.update(PERSONS_TABLE, values, ID_FIELD + "=" + id, null);
                break;
            default:
                throw new IllegalArgumentException("Bad URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return updatedRows;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        int uriType = matcher.match(uri);

        switch (uriType) {
            case PERSONS:
                builder.setTables(PERSONS_TABLE);
                break;
            case PERSON_ID:
                builder.setTables(PERSONS_TABLE);
                builder.appendWhere("id=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Bad URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int uriType = matcher.match(uri);

        int deletedRows = 0;
        switch (uriType) {
            case PERSONS:
                deletedRows = db.delete(PERSONS_TABLE, null, null);
                break;
            case PERSON_ID:
                String id = uri.getLastPathSegment();
                deletedRows = db.delete(PERSONS_TABLE, ID_FIELD + "=" + id, null);
                break;
            default:
                throw new IllegalArgumentException("Bad URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return deletedRows;
    }

    @Override
    public String getType(Uri uri) {
        return Integer.toString(matcher.match(uri));
    }

    private class PersonDBHelper extends SQLiteOpenHelper {
        public PersonDBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + PERSONS_TABLE +
                    " (" + ID_FIELD + " INTEGER PRIMARY KEY AUTOINCREMENT" +
                    ", " + NAME_FIELD + " TEXT NOT NULL UNIQUE" +
                    ", " + VOTES_FIELD + " INTEGER NOT NULL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int o, int n) {
            db.execSQL("DROP TABLE IF EXISTS persons");
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int o, int n) {
            db.execSQL("DROP TABLE IF EXISTS persons");
            onCreate(db);
        }
    }
}
