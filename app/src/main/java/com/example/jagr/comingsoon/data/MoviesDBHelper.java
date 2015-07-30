package com.example.jagr.comingsoon.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.jagr.comingsoon.data.MoviesContract.MovieEntry;
import com.example.jagr.comingsoon.data.MoviesContract.VideoEntry;
import com.example.jagr.comingsoon.data.MoviesContract.ReviewEntry;


/**
 * Created by Ryan Gilreath on 7/30/2015.
 */
public class MoviesDBHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = MoviesDBHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "comingsoon.db";

    private static final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE IF NOT EXISTS " + MovieEntry.TABLE_NAME + " (" +
            MovieEntry._ID + " INTEGER PRIMARY KEY, " +
            MovieEntry.COLUMN_ADULT + " INTEGER NOT NULL, " +
            MovieEntry.COLUMN_BACKDROP_PATH + " TEXT, " +
            MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT, " +
            MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT, " +
            MovieEntry.COLUMN_OVERVIEW + " TEXT, " +
            MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " +
            MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
            MovieEntry.COLUMN_POPULARITY + " REAL, " +
            MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
            MovieEntry.COLUMN_VIDEO + " INTEGER NOT NULL, " +
            MovieEntry.COLUMN_VOTE_AVERAGE + " REAL, " +
            MovieEntry.COLUMN_VOTE_COUNT + " INTEGER);";
    private static final String SQL_DROP_MOVIES_TABLE = "DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME + ";";

    private static final String SQL_CREATE_VIDEOS_TABLE = "CREATE TABLE IF NOT EXISTS " + VideoEntry.TABLE_NAME + " (" +
            VideoEntry._ID + " TEXT PRIMARY KEY, " +
            VideoEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
            VideoEntry.COLUMN_ISO_639_1 + " TEXT, " +
            VideoEntry.COLUMN_KEY + " TEXT NOT NULL, " +
            VideoEntry.COLUMN_NAME + " TEXT NOT NULL, " +
            VideoEntry.COLUMN_SITE + " TEXT, " +
            VideoEntry.COLUMN_SIZE + " INTEGER, " +
            VideoEntry.COLUMN_TYPE + " TEXT, " +
            "FOREIGN KEY ("+ VideoEntry.COLUMN_MOVIE_KEY +") REFERENCES " +
            MovieEntry.TABLE_NAME + " ("+ MovieEntry._ID +") ON DELETE CASCADE);";
    private static final String SQL_DROP_VIDEOS_TABLE = "DROP TABLE IF EXISTS " + VideoEntry.TABLE_NAME + ";";

    private static final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE IF NOT EXISTS " + ReviewEntry.TABLE_NAME + " (" +
            ReviewEntry._ID + " TEXT PRIMARY KEY, " +
            ReviewEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
            ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
            ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
            ReviewEntry.COLUMN_URL + " TEXT, " +
            "FOREIGN KEY ("+ ReviewEntry.COLUMN_MOVIE_KEY +") REFERENCES " +
            MovieEntry.TABLE_NAME + " ("+ MovieEntry._ID +") ON DELETE CASCADE);";
    private static final String SQL_DROP_REVIEWS_TABLE = "DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME + ";";

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_VIDEOS_TABLE);
        db.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_MOVIES_TABLE);
        db.execSQL(SQL_DROP_VIDEOS_TABLE);
        db.execSQL(SQL_DROP_REVIEWS_TABLE);
        onCreate(db);
    }
}
