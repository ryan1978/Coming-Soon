package com.example.jagr.comingsoon.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jagr on 7/26/2015.
 */
public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "com.example.jagr.comingsoon.app.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES   = "movies";
    public static final String PATH_VIDEOS   = "videos";
    public static final String PATH_REVIEWS  = "reviews";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_ADULT             = "adult";
        public static final String COLUMN_BACKDROP_PATH     = "backdrop_path";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_ORIGINAL_TITLE    = "original_title";
        public static final String COLUMN_OVERVIEW          = "overview";
        public static final String COLUMN_RELEASE_DATE      = "release_date";
        public static final String COLUMN_POSTER_PATH       = "poster_path";
        public static final String COLUMN_POPULARITY        = "popularity";
        public static final String COLUMN_TITLE             = "title";
        public static final String COLUMN_VIDEO             = "video";
        public static final String COLUMN_VOTE_AVERAGE      = "vote_average";
        public static final String COLUMN_VOTE_COUNT        = "vote_count";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class VideoEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEOS).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOS;

        public static final String TABLE_NAME = "videos";

        public static final String COLUMN_MOVIE_KEY         = "movie_id";
        public static final String COLUMN_ISO_639_1         = "iso_639_1";
        public static final String COLUMN_KEY               = "key";
        public static final String COLUMN_NAME              = "name";
        public static final String COLUMN_SITE              = "site";
        public static final String COLUMN_SIZE              = "size";
        public static final String COLUMN_TYPE              = "type";

        public static Uri buildVideoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ReviewEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        public static final String TABLE_NAME = "reviews";

        public static final String COLUMN_MOVIE_KEY         = "movie_id";
        public static final String COLUMN_AUTHOR            = "author";
        public static final String COLUMN_CONTENT           = "content";
        public static final String COLUMN_URL               = "url";

        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
