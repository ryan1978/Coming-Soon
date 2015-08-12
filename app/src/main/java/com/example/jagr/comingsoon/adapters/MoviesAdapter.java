package com.example.jagr.comingsoon.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jagr.comingsoon.R;
import com.example.jagr.comingsoon.Utility;
import com.example.jagr.comingsoon.data.MoviesContract.MovieEntry;
import com.example.jagr.comingsoon.data.MoviesDBHelper;
import com.example.jagr.comingsoon.net.TheMovieDbApiRequest;
import com.example.jagr.comingsoon.net.TheMovieDbApiRequest.ApiCall;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by jagr on 7/30/2015.
 */
public class MoviesAdapter extends JSONArrayAdapter {

    private static final String LOG_TAG = MoviesAdapter.class.getSimpleName();

    private Context mContext;
    private int mCurrentPage    = 0;
    private int mPageCount      = 0;
    private boolean mFetching   = false;

    public MoviesAdapter(Context context) {
        mContext = context;
    }

    /**
     * Set the current page of movies
     * @param page The current page number
     */
    public void setCurrentPage(int page) {
        mCurrentPage = page;
    }
    public int getCurrentPage() { return mCurrentPage; }
    /**
     * Set the total number of pages that can be retrieved
     * @param count The total number of pages that can be retrieved
     */
    public void setPageCount(int count) {
        mPageCount = count;
    }
    public int getPageCount() {
        return mPageCount;
    }
    /**
     * Sets all of the data for this adapter so it can restore from instance state
     * @param page The current page number
     * @param count The total number of pages that can be retrieved
     * @param data The JSONArray of data this adapter uses
     */
    public void setData(int page, int count, JSONArray data) {
        setCurrentPage(page);
        setPageCount(count);
        setData(data);
        notifyDataSetChanged();
    }

    /**
     * Set the flag that indicates if the adapter is currently fetching data from the server
     * @param isFetching
     */
    public void setFetching(boolean isFetching) {
        mFetching = isFetching;
    }
    public boolean getFetching() {
        return mFetching;
    }

    /**
     * Adds movies to the JSONArray of data
     * @param movies A JSONArray of movie JSONObjects as retrieved from the MovieDbApi call
     */
    public void addMovies(JSONArray movies) {
        if (movies != null && movies != JSONObject.NULL) {
            for (int i = 0; i < movies.length(); i++) {
                mData.put(movies.optJSONObject(i));
            }
        }
    }

    /**
     * Returns a boolean indicating if more pages can be fetched
     * @return boolean indicating if more pages can be fetched
     */
    public boolean hasMorePages() {
        return mCurrentPage < mPageCount;
    }

    @Override
    public void empty() {
        mCurrentPage = 0;
        mPageCount = 0;
        super.empty();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JSONObject movie        = mData.optJSONObject(position);
        String posterPath       = movie.optString("poster_path");
        String movieTitle       = movie.optString("title");

        if (convertView == null) {
            LayoutInflater vi = LayoutInflater.from(mContext);
            convertView = vi.inflate(R.layout.grid_item_movie, parent, false);
        }

        ImageView poster    = (ImageView) convertView.findViewById(R.id.poster_image);
        TextView title      = (TextView) convertView.findViewById(R.id.movie_title);

        if (posterPath.trim().length() > 0) {
            Picasso
                    .with(mContext)
                    .load("http://image.tmdb.org/t/p/" + Utility.getImageFolder(mContext) + posterPath)
                    .placeholder(Utility.getImagePlaceholderId(mContext))
                    .fit()
                    .centerCrop()
                    .into(poster);
        }
        title.setText(movieTitle);

        return convertView;
    }

    /**
     * Retrieve all the favorited movies from the database
     */
    private void fetchFavorites() {
        final JSONArray data    = new JSONArray();

        final SQLiteDatabase db = new MoviesDBHelper(mContext).getReadableDatabase();
        final Cursor cursor = db.query(
                MovieEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                MovieEntry.COLUMN_TITLE
        );

        while (cursor.moveToNext()) {
            JSONObject movie = new JSONObject();
            try {
                movie.put("id",
                        cursor.getLong(cursor.getColumnIndex(MovieEntry._ID)));
                movie.put(MovieEntry.COLUMN_ADULT,
                        cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_ADULT)) == 1);
                movie.put(MovieEntry.COLUMN_BACKDROP_PATH,
                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_BACKDROP_PATH)));
                movie.put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE,
                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_LANGUAGE)));
                movie.put(MovieEntry.COLUMN_ORIGINAL_TITLE,
                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE)));
                movie.put(MovieEntry.COLUMN_OVERVIEW,
                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW)));
                movie.put(MovieEntry.COLUMN_RELEASE_DATE,
                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE)));
                movie.put(MovieEntry.COLUMN_POSTER_PATH,
                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH)));
                movie.put(MovieEntry.COLUMN_POPULARITY,
                        cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_POPULARITY)));
                movie.put(MovieEntry.COLUMN_TITLE,
                        cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE)));
                movie.put(MovieEntry.COLUMN_VIDEO,
                        cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_VIDEO)) == 1);
                movie.put(MovieEntry.COLUMN_VOTE_AVERAGE,
                        cursor.getFloat(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE)));
                movie.put(MovieEntry.COLUMN_VOTE_COUNT,
                        cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_COUNT)));

                // Replace null values with JSONObject.NULL
                Iterator<String> movieKeys = movie.keys();
                while (movieKeys.hasNext()) {
                    String k = movieKeys.next();
                    if (movie.isNull(k)) movie.put(k, JSONObject.NULL);
                }

                data.put(movie);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setData(1, 1, data);
        setFetching(false);
    }

    /**
     * Retrieve the next page of movies from the MovieDbApi
     */
    public void fetchMovies() {
        if (!getFetching()) {
            setFetching(true);

            if (Utility.getPreferredSorting(mContext).equals(mContext.getResources().getString(R.string.pref_sort_favorite))) {
                fetchFavorites();
            } else {
                FetchMoviesTask task = new FetchMoviesTask(mContext, this);
                task.execute();
            }
        }
    }

    /**
     * Class responsible for making api requests to fetch movies to load into the adapter
     */
    private static class FetchMoviesTask extends AsyncTask<Void, Void, JSONObject> {

        private WeakReference<Context> mContext;
        private WeakReference<MoviesAdapter> mAdapter;

        public FetchMoviesTask(Context context, MoviesAdapter adapter) {
            super();
            mContext = new WeakReference<>(context);
            mAdapter = new WeakReference<>(adapter);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject response     = null;
            Context context         = mContext.get();
            MoviesAdapter adapter   = mAdapter.get();

            if (context != null && adapter != null) {
                TheMovieDbApiRequest request = new TheMovieDbApiRequest(context, getEndPoint(), null);

                HashMap<String, String> queryParams = new HashMap<>();
                queryParams.put("page", String.valueOf(adapter.getCurrentPage() + 1));
                queryParams.put("sort_by", Utility.getPreferredSorting(context));

                response = request.doAPIRequest(queryParams);
            }

            return response;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            MoviesAdapter adapter = mAdapter.get();

            if (adapter != null && response != null && response.keys().hasNext()) {
                int page            = response.optInt("page");
                int pageCount       = response.optInt("total_pages");
                JSONArray movies    = response.optJSONArray("results");
                JSONArray cleaned   = new JSONArray();

                // Filter out movies w/o a poster_path
                for (int i = 0; i < movies.length(); i++) {
                    JSONObject movie = movies.optJSONObject(i);
                    if (!movie.isNull("poster_path") && !movie.optString("poster_path").equals("")) {
                        cleaned.put(movies.optJSONObject(i));
                    }
                }

                adapter.setCurrentPage(page);
                adapter.setPageCount(pageCount > 1000 ? 1000 : pageCount);
                adapter.addMovies(cleaned);
                adapter.notifyDataSetChanged();
                adapter.setFetching(false);
            }
        }

        private ApiCall getEndPoint() {
            ApiCall result = ApiCall.DISCOVER_MOVIES;
            Context context = mContext.get();

            if (context != null) {
                final String sortPref = Utility.getPreferredSorting(context);
                if (sortPref.equals(context.getResources().getString(R.string.pref_sort_now_playing))) {
                    result = ApiCall.NOWPLAYING_MOVIES;
                } else if (sortPref.equals(context.getResources().getString(R.string.pref_sort_upcoming))) {
                    result = ApiCall.UPCOMING_MOVIES;
                }
            }

            return result;
        }
    }
}
