package com.example.jagr.comingsoon.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.jagr.comingsoon.data.MoviesContract.MovieEntry;
import com.example.jagr.comingsoon.data.MoviesContract.VideoEntry;
import com.example.jagr.comingsoon.data.MoviesContract.ReviewEntry;

/**
 * Created by Ryan Gilreath on 7/30/2015.
 */
public class MoviesProvider extends ContentProvider {

    protected static final int MOVIE            = 100;
    protected static final int VIDEO            = 200;
    protected static final int REVIEW           = 300;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDBHelper mOpenHelper;

    protected static UriMatcher buildUriMatcher() {
        final UriMatcher matcher    = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority      = MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIE);
        matcher.addURI(authority, MoviesContract.PATH_VIDEOS, VIDEO);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS, REVIEW);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case VIDEO:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case REVIEW:
                return MoviesContract.VideoEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                retCursor = db.query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case VIDEO: {
                retCursor = db.query(
                        VideoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case REVIEW: {
                retCursor = db.query(
                        ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = MovieEntry.buildMovieUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case VIDEO: {
                long _id = db.insert(VideoEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = VideoEntry.buildVideoUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case REVIEW: {
                long _id = db.insert(ReviewEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = ReviewEntry.buildReviewUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (selection == null) selection = "1";

        switch (match) {
            case MOVIE: {
                rowsDeleted = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case VIDEO: {
                rowsDeleted = db.delete(VideoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case REVIEW: {
                rowsDeleted = db.delete(ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE: {
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case VIDEO: {
                rowsUpdated = db.update(VideoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case REVIEW: {
                rowsUpdated = db.update(ReviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch(match) {
            case MOVIE: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) returnCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case VIDEO: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(VideoEntry.TABLE_NAME, null, value);
                        if (_id != -1) returnCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case REVIEW: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ReviewEntry.TABLE_NAME, null, value);
                        if (_id != -1) returnCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
