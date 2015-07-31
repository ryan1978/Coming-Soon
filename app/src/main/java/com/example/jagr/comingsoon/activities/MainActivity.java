package com.example.jagr.comingsoon.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.jagr.comingsoon.FetchMoviesTask;
import com.example.jagr.comingsoon.R;
import com.example.jagr.comingsoon.Utility;
import com.example.jagr.comingsoon.adapters.MoviesAdapter;

// TODO: Unfuck this activity as far as data retrieval and OutOfMemory errors
// TODO: Convert this to a fragment
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String MOVIE_EXTRA          = "movie";
    private static final String STATE_SELECTION     = "selection";
    private static final String STATE_PAGE          = "page";
    private static final String STATE_PAGE_COUNT    = "page_count";
    private static final String STATE_DATA          = "data";

    private AlertDialog mAPIKeyAlert;
    private MoviesAdapter mAdapter;
    private GridView mMoviesGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter    = new MoviesAdapter(this);

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
                new FetchMoviesTask(this, mAdapter).execute();
            }
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
    public void onStart() {
        Log.d(LOG_TAG, "onStart");
        super.onStart();

        if (!Utility.isApiKeySet(this)) {
            showApiKeyAlert();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAPIKeyAlert != null && mAPIKeyAlert.isShowing()) {
            mAPIKeyAlert.dismiss();
        }
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
        outState.putInt(STATE_SELECTION, mMoviesGrid.getFirstVisiblePosition());
        outState.putInt(STATE_PAGE, mAdapter.getCurrentPage());
        outState.putInt(STATE_PAGE_COUNT, mAdapter.getPageCount());
        outState.putString(STATE_DATA, mAdapter.getData().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAdapter.setCurrentPage(savedInstanceState.getInt(STATE_PAGE));
        mAdapter.setPageCount(savedInstanceState.getInt(STATE_PAGE_COUNT));
        mAdapter.setData(savedInstanceState.getString(STATE_DATA));
        mMoviesGrid.setSelection(savedInstanceState.getInt(STATE_SELECTION));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        // If the user changes the api key or sort settings attempt to refetch movies
        if (key.equals(getString(R.string.pref_api_key_key))
                || key.equals(getString(R.string.pref_sort_key))) {
            if (Utility.isApiKeySet(this)) {
                mAdapter.empty();

                new FetchMoviesTask(this, mAdapter).execute();

                if (mAPIKeyAlert != null && mAPIKeyAlert.isShowing()) {
                    mAPIKeyAlert.dismiss();
                }
            }
        }
    }

    /**
     * Shows an alert dialog telling the user they must enter an API key to use the app
     */
    private void showApiKeyAlert() {
        mAPIKeyAlert = new AlertDialog.Builder(this)
                .setTitle("Notice")
                .setMessage("To use this application you must have a themoviedb.org API key set!")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Log.d(LOG_TAG, "canceled!");
                        finish();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        Log.d(LOG_TAG, "dismissed!");
                        mAPIKeyAlert = null;
                    }
                })
                .setPositiveButton("Enter Key", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked enter key button
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked close button
                        finish();
                    }
                })
                .create();

        mAPIKeyAlert.show();
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
            if (mUserScrolled && mAdapter.hasMorePages()) {
                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem >= mAdapter.getCount() - 10) {
                    new FetchMoviesTask(MainActivity.this, mAdapter).execute();
                }
            }
        }
    }

    // TODO: Move this function somewhere else and delete this inner class
    /*
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
    */
}
