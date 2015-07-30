package com.example.jagr.comingsoon.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.jagr.comingsoon.R;
import com.example.jagr.comingsoon.Utility;
import com.example.jagr.comingsoon.data.MoviesContract.MovieEntry;
import com.example.jagr.comingsoon.data.MoviesDBHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String MOVIE_EXTRA          = "movie";
    private static final String STATE_SELECTION     = "selection";
    private static final String STATE_PAGE          = "page";
    private static final String STATE_PAGE_COUNT    = "page_count";
    private static final String STATE_DATA          = "data";

    private MovieAdapter mAdapter;
    private GridView mMoviesGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new MovieAdapter(this);

        mMoviesGrid = (GridView) findViewById(R.id.movies_grid);
        mMoviesGrid.setAdapter(mAdapter);
        mMoviesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra(MOVIE_EXTRA, mAdapter.getItem(position).toString());
                startActivity(intent);
            }
        });
        mMoviesGrid.setOnScrollListener(new MovieScrollWatcher());

        // Only want to attempt to fetch data if api key is set or activity is not resuming
        if (Utility.isApiKeySet(this)) {
            if (savedInstanceState == null) {
                mAdapter.clearData();
                mAdapter.fetch();
            }
        } else {
            showApiKeyAlert();
        }

        // Register this activity as a listener for shared pref changes
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTION,    mMoviesGrid.getFirstVisiblePosition());
        outState.putInt(STATE_PAGE,         mAdapter.getCurrentPage());
        outState.putInt(STATE_PAGE_COUNT, mAdapter.getPageCount());
        outState.putString(STATE_DATA, mAdapter.getData().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mAdapter.setData(savedInstanceState.getInt(STATE_PAGE),
                savedInstanceState.getInt(STATE_PAGE_COUNT),
                savedInstanceState.getString(STATE_DATA));
        mMoviesGrid.setSelection(savedInstanceState.getInt(STATE_SELECTION));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        // If the user changes the api key or sort settings attempt to refetch movies
        if (key.equals(getString(R.string.pref_api_key_key))
                || key.equals(getString(R.string.pref_sort_key))) {
            if (Utility.isApiKeySet(this)) {
                mAdapter.clearData();
                mAdapter.fetch();
            } else {
                showApiKeyAlert();
            }
        }
    }

    /**
     * Shows an alert dialog telling the user they must enter an API key to use the app
     */
    private void showApiKeyAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Notice")
                .setMessage("To use this application you must have a themoviedb.org API key set!")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Log.d(LOG_TAG, "canceled!");
                        dialog.dismiss();
                        finish();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        Log.d(LOG_TAG, "dismissed!");
                    }
                })
                .setPositiveButton("Enter Key", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked enter key button
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked close button
                        dialog.dismiss();
                        finish();
                    }
                })
                .create()
                .show();
    }

    /**
     * Class to watch for the GridView being scrolled to the bottom that will
     * fetch and append additional results
     */
    private class MovieScrollWatcher implements GridView.OnScrollListener {
        private boolean mUserScrolled = false;

        @Override
        public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                mUserScrolled = true;
            }
        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            // As the grid is scrolled towards the bottom fetch more movies
            if (mUserScrolled && !mAdapter.isFetching() && mAdapter.hasMorePages()) {
                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem >= mAdapter.getCount() - 10) {
                    mAdapter.fetch();
                }
            }
        }
    }

    /**
     * Custom adapter class to populate gridview
     */
    private static class MovieAdapter extends BaseAdapter {
        private static final String LOG_TAG = MovieAdapter.class.getSimpleName();
        private final WeakReference<Activity> mActivity;
        private int mCurrentPage    = 0;
        private int mTotalPages     = 0;
        private JSONArray mMovies   = new JSONArray();
        private boolean mFetching   = false;

        public MovieAdapter(Activity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        public int getCount() {
            return mMovies.length();
        }

        @Override
        public Object getItem(int position) {
            return mMovies.optJSONObject(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            JSONObject movie        = mMovies.optJSONObject(position);
            String posterPath       = movie.optString("poster_path");
            Activity activity       = mActivity.get();

            if (activity != null) {
                if (convertView == null) {
                    LayoutInflater vi = LayoutInflater.from(activity);
                    convertView = vi.inflate(R.layout.grid_item_movie, parent, false);
                }

                ImageView poster = (ImageView) convertView.findViewById(R.id.poster_image);

                if (posterPath.trim().length() > 0) {
                    Picasso.with(activity).load("http://image.tmdb.org/t/p/w185" + posterPath).into(poster);
                }
            }

            return convertView;
        }

        // Getters used for saving state
        public JSONArray getData()  { return mMovies; }
        public int getCurrentPage() { return mCurrentPage; }
        public int getPageCount()   { return mTotalPages; }

        /**
         * Used when the state is restored
         * @param page The current page
         * @param pageCount The total number of pages
         * @param data The JSON serializable string of movies
         */
        public void setData(int page, int pageCount, String data) {
            try {
                mCurrentPage    = page;
                mTotalPages     = pageCount;
                mMovies         = new JSONArray(data);
                notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.toString());
                e.printStackTrace();
            }
        }

        /**
         * Resets the adapter and empties ListView
         */
        public void clearData() {
            mCurrentPage    = 0;
            mTotalPages     = 0;
            mMovies         = new JSONArray();
            notifyDataSetChanged();
        }

        /**
         * Returns a boolean indicating if more pages can be fetched
         * @return boolean indicating if more pages can be fetched
         */
        public boolean hasMorePages() {
            return mCurrentPage < mTotalPages;
        }

        /**
         * Returns boolean indicating if a http fetch is in progress
         * @return boolean indicating if the adapter is currently in the middle of a fetch
         */
        public boolean isFetching() {
            return mFetching;
        }

        /**
         * Attempts to fetch movie records from the api
         */
        public void fetch() {
            final Activity activity = mActivity.get();

            if (activity != null && !mFetching) {
                String sortingPref = Utility.getPreferredSorting(activity);
                if (sortingPref != null && sortingPref.equals(activity.getString(R.string.pref_sort_favorite))) {
                    fetchFavorites();
                } else {
                    FetchMoviesTask fetcher = new FetchMoviesTask(MovieAdapter.this);
                    fetcher.execute();
                }
            }
        }

        private void fetchFavorites() {
            final Activity activity = mActivity.get();
            final JSONArray data    = new JSONArray();

            if (activity != null) {
                final SQLiteDatabase db = new MoviesDBHelper(activity).getReadableDatabase();
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
                        Log.e(LOG_TAG, e.toString());
                        e.printStackTrace();
                    }
                }

                Log.d(LOG_TAG, "Favorites: " + data.toString());
            }

            mCurrentPage = 1;
            mTotalPages = 1;
            mMovies = data;
            notifyDataSetChanged();
        }

        private static class FetchMoviesTask extends AsyncTask<Void, Void, String> {
            private static final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
            private final WeakReference<MovieAdapter> mAdapter;

            FetchMoviesTask(MovieAdapter adapter) {
                mAdapter = new WeakReference<>(adapter);
            }

            @Override
            public void onPreExecute() {
                MovieAdapter adapter = mAdapter.get();
                if (adapter != null) {
                    adapter.mFetching = true;
                }
            }

            @Override
            public String doInBackground(Void... params) {
                String jsonResponse         = null;
                final MovieAdapter adapter  = mAdapter.get();

                if (adapter != null) {
                    HttpURLConnection httpConn  = null;
                    BufferedReader reader       = null;

                    try {
                        final String MOVIEDB_BASE_URL   = "http://api.themoviedb.org/3/discover/movie?";
                        final String PAGE_PARAM         = "page";
                        final String SORT_PARAM         = "sort_by";
                        final String KEY_PARAM          = "api_key";

                        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                                .appendQueryParameter(PAGE_PARAM, adapter.mCurrentPage == 0 ? "1" : String.valueOf(adapter.mCurrentPage + 1))
                                .appendQueryParameter(SORT_PARAM, Utility.getPreferredSorting(adapter.mActivity.get()))
                                .appendQueryParameter(KEY_PARAM, Utility.getApiKey(adapter.mActivity.get()))
                                .build();

                        // TODO: If sort is by highest rated, add vote_count.gte=?? param to URI

                        URL url = new URL(builtUri.toString());

                        Log.d(LOG_TAG, url.toString());

                        httpConn = (HttpURLConnection) url.openConnection();
                        httpConn.setRequestMethod("GET");
                        httpConn.connect();

                        InputStream inputStream = httpConn.getInputStream();
                        StringBuilder buffer = new StringBuilder();
                        if (inputStream == null) {
                            return null;
                        }
                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line);
                        }

                        if (buffer.length() == 0) {
                            return null;
                        }

                        jsonResponse = buffer.toString();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error", e);
                        e.printStackTrace();
                    } finally {
                        if (httpConn != null) {
                            httpConn.disconnect();
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (final IOException e) {
                                Log.e(LOG_TAG, "Error closing stream", e);
                            }
                        }
                    }
                }

                return jsonResponse;
            }

            @Override
            public void onPostExecute(String jsonResponse) {
                MovieAdapter adapter = mAdapter.get();

                if (jsonResponse != null) {
                    if (adapter != null) {
                        try {
                            JSONObject json = new JSONObject(jsonResponse);
                            JSONArray results = json.optJSONArray("results");
                            int page = json.optInt("page");
                            int totalPages = json.optInt("total_pages");

                            adapter.mCurrentPage = page;
                            adapter.mTotalPages = totalPages < 1000 ? totalPages : 1000;

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject movie = results.optJSONObject(i);

                                // Filter out movies w/o a poster_path
                                if (!movie.isNull("poster_path") && !movie.optString("poster_path").equals("")) {
                                    adapter.mMovies.put(results.optJSONObject(i));
                                }
                            }

                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.toString(), e);
                            e.printStackTrace();
                        }
                    }
                }

                adapter.mFetching = false;
            }
        }
    }
}
