package com.example.jagr.comingsoon.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jagr.comingsoon.R;
import com.example.jagr.comingsoon.Utility;
import com.example.jagr.comingsoon.data.MoviesContract;
import com.example.jagr.comingsoon.data.MoviesContract.MovieEntry;
import com.example.jagr.comingsoon.data.MoviesContract.VideoEntry;
import com.example.jagr.comingsoon.data.MoviesContract.ReviewEntry;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragment extends Fragment {

    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();

    public static final String MOVIE_EXTRA              = "movie";
    private static final String STATE_VIDEO_DATA        = "video_data";
    private static final String STATE_REVIEW_DATA       = "review_data";

    private JSONObject mMovie;
    private VideoPagerAdapter mVideoAdapter;
    private ReviewPagerAdapter mReviewAdapter;

    public DetailsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startIntent = getActivity().getIntent();
        if (startIntent.hasExtra(MOVIE_EXTRA)) {
            try {
                mMovie = new JSONObject(startIntent.getStringExtra(MOVIE_EXTRA));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_details, menu);

        if (isFavorite(mMovie)) {
            MenuItem item = menu.findItem(R.id.action_favorite);
            if (item != null) {
                item.setIcon(R.drawable.ic_action_action_favorite);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favorite) {
            if (isFavorite(mMovie)) {
                if (removeFavorite(mMovie)) {
                    item.setIcon(R.drawable.ic_action_action_favorite_outline);
                }
            } else if (addFavorite(mMovie)) {
                item.setIcon(R.drawable.ic_action_action_favorite);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_VIDEO_DATA, mVideoAdapter.getData().toString());
        outState.putString(STATE_REVIEW_DATA, mReviewAdapter.getData().toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            try {
                mMovie = new JSONObject(arguments.getString(MOVIE_EXTRA));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        final View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        if (mMovie != null) {
            mVideoAdapter = new VideoPagerAdapter(getActivity(), mMovie.optInt("id"));
            mReviewAdapter = new ReviewPagerAdapter(getActivity(), mMovie.optInt("id"));
            if (savedInstanceState != null) {
                mVideoAdapter.setData(savedInstanceState.getString(STATE_VIDEO_DATA));
                mReviewAdapter.setData(savedInstanceState.getString(STATE_REVIEW_DATA));
            } else {
                mVideoAdapter.fetch();
                mReviewAdapter.fetch();
            }

            final ImageView backdrop    = (ImageView) rootView.findViewById(R.id.backdrop_image);
            final ImageView poster      = (ImageView) rootView.findViewById(R.id.poster_image);
            final TextView title        = (TextView) rootView.findViewById(R.id.original_title);
            final TextView overview     = (TextView) rootView.findViewById(R.id.overview);
            final TextView rating       = (TextView) rootView.findViewById(R.id.vote_average);
            final TextView release      = (TextView) rootView.findViewById(R.id.release_date);
            final ViewPager videos      = (ViewPager) rootView.findViewById(R.id.video_list);
            final ViewPager reviews     = (ViewPager) rootView.findViewById(R.id.review_list);

            videos.setAdapter(mVideoAdapter);
            reviews.setAdapter(mReviewAdapter);

            if (!mMovie.isNull(MovieEntry.COLUMN_BACKDROP_PATH) && !mMovie.optString(MovieEntry.COLUMN_BACKDROP_PATH).equals("")) {
                Picasso
                        .with(getActivity())
                        .load("http://image.tmdb.org/t/p/w780" + mMovie.optString(MovieEntry.COLUMN_BACKDROP_PATH))
                        .placeholder(R.drawable.placeholder_w780)
                        .fit()
                        .centerCrop()
                        .into(backdrop);
            } else if (!mMovie.isNull(MovieEntry.COLUMN_POSTER_PATH) && !mMovie.optString(MovieEntry.COLUMN_POSTER_PATH).equals("")) {
                Picasso
                        .with(getActivity())
                        .load("http://image.tmdb.org/t/p/w780" + mMovie.optString(MovieEntry.COLUMN_POSTER_PATH))
                        .placeholder(R.drawable.placeholder_w780)
                        .fit()
                        .centerCrop()
                        .into(backdrop);
            } else {
                Picasso
                        .with(getActivity())
                        .load(R.drawable.placeholder_w780)
                        .fit()
                        .centerCrop()
                        .into(backdrop);
            }
            if (!mMovie.isNull(MovieEntry.COLUMN_POSTER_PATH) && !mMovie.optString(MovieEntry.COLUMN_POSTER_PATH).equals("")) {
                Picasso
                        .with(getActivity())
                        .load("http://image.tmdb.org/t/p/" + Utility.getImageFolder(getActivity()) + mMovie.optString(MovieEntry.COLUMN_POSTER_PATH))
                        .placeholder(Utility.getImagePlaceholderId(getActivity()))
                        .fit()
                        .centerCrop()
                        .into(poster);
            } else {
                Picasso
                        .with(getActivity())
                        .load(Utility.getImagePlaceholderId(getActivity()))
                        .fit()
                        .centerCrop()
                        .into(poster);
            }

            title.setText(mMovie.isNull(MovieEntry.COLUMN_TITLE) ? "Not Available" : mMovie.optString(MovieEntry.COLUMN_TITLE));
            overview.setText(mMovie.isNull(MovieEntry.COLUMN_OVERVIEW) ? "Not Available" : mMovie.optString(MovieEntry.COLUMN_OVERVIEW));
            rating.setText("Rating: " + (mMovie.isNull(MovieEntry.COLUMN_VOTE_AVERAGE) ? "Not Available" : (int) Math.round(Double.valueOf(mMovie.optString(MovieEntry.COLUMN_VOTE_AVERAGE))) + "/10"));
            release.setText("Released: " + (mMovie.isNull(MovieEntry.COLUMN_RELEASE_DATE) ? "Not Available" : reformatDate(mMovie.optString(MovieEntry.COLUMN_RELEASE_DATE))));
        }

        return rootView;
    }

    /**
     * Converts the date returned from TheMovieDbAPI (YYYY-MM-DD) to MM/DD/YY
     * @param date
     * @return
     */
    private String reformatDate(String date) {
        String result = date;

        if (date != JSONObject.NULL && date != null && date.trim().length() > 0) {
            String[] dateParts = date.trim().split("-");
            String month    = String.valueOf(Integer.parseInt(dateParts[1]));
            String day      = String.valueOf(Integer.parseInt(dateParts[2]));
            String year     = String.valueOf(Integer.parseInt(dateParts[0]));

            result = month + "/" + day + "/" + year.substring(year.length() - 2);
        }

        return result;
    }

    /**
     * Custom adapter class to populate videos list view
     */
    private static class VideoPagerAdapter extends PagerAdapter {

        private static final String LOG_TAG = VideoPagerAdapter.class.getSimpleName();
        private WeakReference<Activity> mActivity;
        private int mMovieId;
        private JSONArray mVideos   = new JSONArray();

        public VideoPagerAdapter(Activity activity, int movieId) {
            mActivity   = new WeakReference<>(activity);
            mMovieId    = movieId;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view           = null;
            final Activity activity   = mActivity.get();

            if (activity != null) {
                final JSONObject video = mVideos.optJSONObject(position);

                LayoutInflater inflater = LayoutInflater.from(mActivity.get());
                view = inflater.inflate(R.layout.page_video, null);

                ImageView frame = (ImageView) view.findViewById(R.id.video_preview);
                frame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent videoPlay = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + video.optString(VideoEntry.COLUMN_KEY)));
                        activity.startActivity(videoPlay);
                    }
                });
                Picasso.with(activity)
                        .load("http://img.youtube.com/vi/" + video.optString(VideoEntry.COLUMN_KEY) + "/0.jpg")
                        .fit()
                        .centerCrop()
                        .into(frame);

                container.addView(view);
            }

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mVideos.length();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public float getPageWidth(int position) {
            return 0.5f;
        }

        // Getters used for saving state
        public JSONArray getData()  { return mVideos; }

        /**
         * Used when the state is restored
         * @param data The JSON serializable string of videos
         */
        public void setData(String data) {
            try {
                mVideos = new JSONArray(data);
                notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.toString());
                e.printStackTrace();
            }
        }

        /**
         * Attempts to fetch movie records from the api
         */
        public void fetch() {
            FetchVideosTask fetcher = new FetchVideosTask(VideoPagerAdapter.this);
            fetcher.execute();
        }

        private static class FetchVideosTask extends AsyncTask<Void, Void, String> {
            private static final String LOG_TAG = FetchVideosTask.class.getSimpleName();
            private final WeakReference<VideoPagerAdapter> mAdapter;

            FetchVideosTask(VideoPagerAdapter adapter) {
                mAdapter = new WeakReference<>(adapter);
            }

            @Override
            public void onPreExecute() { }

            @Override
            public String doInBackground(Void... params) {
                String jsonResponse                 = null;
                final VideoPagerAdapter adapter     = mAdapter.get();

                if (adapter != null) {
                    HttpURLConnection httpConn  = null;
                    BufferedReader reader       = null;

                    try {
                        final String MOVIEDB_BASE_URL   = "http://api.themoviedb.org/3/movie/"+ adapter.mMovieId +"/videos?";
                        final String KEY_PARAM          = "api_key";

                        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                                .appendQueryParameter(KEY_PARAM, Utility.getApiKey(adapter.mActivity.get()))
                                .build();

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
                VideoPagerAdapter adapter = mAdapter.get();

                if (jsonResponse != null) {
                    if (adapter != null) {
                        try {
                            JSONObject json = new JSONObject(jsonResponse);
                            JSONArray results = json.optJSONArray("results");

                            for (int i = 0; i < results.length(); i++) {
                                adapter.mVideos.put(results.optJSONObject(i));
                            }

                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.toString(), e);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Custom adapter class to populate reviews list view
     */
    private static class ReviewPagerAdapter extends PagerAdapter {

        private static final String LOG_TAG = ReviewPagerAdapter.class.getSimpleName();
        private WeakReference<Activity> mActivity;
        private int mMovieId;
        private JSONArray mReviews  = new JSONArray();

        public ReviewPagerAdapter(Activity activity, int movieId) {
            mActivity   = new WeakReference<>(activity);
            mMovieId    = movieId;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view           = null;
            Activity activity   = mActivity.get();

            if (activity != null) {
                final JSONObject review = mReviews.optJSONObject(position);

                LayoutInflater inflater = LayoutInflater.from(mActivity.get());
                view = inflater.inflate(R.layout.page_review, null);

                TextView reviewer   = (TextView) view.findViewById(R.id.reviewer_name);
                TextView content    = (TextView) view.findViewById(R.id.review_text);
                reviewer.setText(review.optString(ReviewEntry.COLUMN_AUTHOR));
                content.setText(review.optString(ReviewEntry.COLUMN_CONTENT));

                container.addView(view);
            }

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mReviews.length();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        // Getters used for saving state
        public JSONArray getData()  { return mReviews; }

        /**
         * Used when the state is restored
         * @param data The JSON serializable string of movies
         */
        public void setData(String data) {
            try {
                mReviews         = new JSONArray(data);
                notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.toString());
                e.printStackTrace();
            }
        }

        /**
         * Attempts to fetch movie records from the api
         */
        public void fetch() {
            FetchReviewsTask fetcher = new FetchReviewsTask(ReviewPagerAdapter.this);
            fetcher.execute();
        }

        private static class FetchReviewsTask extends AsyncTask<Void, Void, String> {
            private static final String LOG_TAG = FetchReviewsTask.class.getSimpleName();
            private final WeakReference<ReviewPagerAdapter> mAdapter;

            FetchReviewsTask(ReviewPagerAdapter adapter) {
                mAdapter = new WeakReference<>(adapter);
            }

            @Override
            public void onPreExecute() { }

            @Override
            public String doInBackground(Void... params) {
                String jsonResponse                 = null;
                final ReviewPagerAdapter adapter    = mAdapter.get();

                if (adapter != null) {
                    HttpURLConnection httpConn  = null;
                    BufferedReader reader       = null;

                    try {
                        final String MOVIEDB_BASE_URL   = "http://api.themoviedb.org/3/movie/"+ adapter.mMovieId +"/reviews?";
                        final String KEY_PARAM          = "api_key";

                        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                                .appendQueryParameter(KEY_PARAM, Utility.getApiKey(adapter.mActivity.get()))
                                .build();

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
                ReviewPagerAdapter adapter = mAdapter.get();

                if (jsonResponse != null) {
                    if (adapter != null) {
                        try {
                            JSONObject json = new JSONObject(jsonResponse);
                            JSONArray results = json.optJSONArray("results");

                            for (int i = 0; i < results.length(); i++) {
                                adapter.mReviews.put(results.optJSONObject(i));
                            }

                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.toString(), e);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public boolean isFavorite(JSONObject movie) {
        boolean result = false;

        if (movie != null) {
            final int movieId = movie.optInt("id");

            if (movieId > 0) {
                final SQLiteDatabase db = new MoviesDBHelper(getActivity()).getReadableDatabase();
                Cursor cursor = db.query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        new String[] { MoviesContract.MovieEntry._ID },
                        MoviesContract.MovieEntry._ID + " = ?",
                        new String[] { String.valueOf(movieId) },
                        null,
                        null,
                        null
                );
                result = cursor.getCount() > 0;
                cursor.close();
                db.close();
            }
        }

        return result;
    }

    public boolean addFavorite(JSONObject movie) {
        boolean result = false;

        if (movie != null) {
            if (!isFavorite(movie)) {
                final SQLiteDatabase db = new MoviesDBHelper(getActivity()).getWritableDatabase();
                final ContentValues values = new ContentValues();
                values.put(MovieEntry._ID, movie.optLong("id"));
                values.put(MovieEntry.COLUMN_ADULT, movie.optBoolean(MovieEntry.COLUMN_ADULT) ? 1 : 0);
                values.put(MovieEntry.COLUMN_BACKDROP_PATH,
                        movie.isNull(MovieEntry.COLUMN_BACKDROP_PATH) ? null : movie.optString(MovieEntry.COLUMN_BACKDROP_PATH));
                values.put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE,
                        movie.isNull(MovieEntry.COLUMN_ORIGINAL_LANGUAGE) ? null : movie.optString(MovieEntry.COLUMN_ORIGINAL_LANGUAGE));
                values.put(MovieEntry.COLUMN_ORIGINAL_TITLE,
                        movie.isNull(MovieEntry.COLUMN_ORIGINAL_TITLE) ? null : movie.optString(MovieEntry.COLUMN_ORIGINAL_TITLE));
                values.put(MovieEntry.COLUMN_OVERVIEW,
                        movie.isNull(MovieEntry.COLUMN_OVERVIEW) ? null : movie.optString(MovieEntry.COLUMN_OVERVIEW));
                values.put(MovieEntry.COLUMN_RELEASE_DATE, movie.optString(MovieEntry.COLUMN_RELEASE_DATE));
                values.put(MovieEntry.COLUMN_POSTER_PATH,
                        movie.isNull(MovieEntry.COLUMN_POSTER_PATH) ? null : movie.optString(MovieEntry.COLUMN_POSTER_PATH));
                values.put(MovieEntry.COLUMN_POPULARITY, movie.optDouble(MovieEntry.COLUMN_POPULARITY));
                values.put(MovieEntry.COLUMN_TITLE,
                        movie.isNull(MovieEntry.COLUMN_TITLE) ? null : movie.optString(MovieEntry.COLUMN_TITLE));
                values.put(MovieEntry.COLUMN_VIDEO, movie.optBoolean(MovieEntry.COLUMN_VIDEO) ? 1 : 0);
                values.put(MovieEntry.COLUMN_VOTE_AVERAGE, movie.optDouble(MovieEntry.COLUMN_VOTE_AVERAGE));
                values.put(MovieEntry.COLUMN_VOTE_COUNT, movie.optInt(MovieEntry.COLUMN_VOTE_COUNT));

                long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                result = _id > 0;
                db.close();
            }
        }

        return result;
    }

    public boolean removeFavorite(JSONObject movie) {
        boolean result = false;

        if (movie != null) {
            if (isFavorite(movie)) {
                final int movieId = movie.optInt("id");

                if (movieId > 0) {
                    final SQLiteDatabase db = new MoviesDBHelper(getActivity()).getWritableDatabase();
                    int rowsDeleted = db.delete(
                            MovieEntry.TABLE_NAME,
                            MovieEntry._ID + " = ?",
                            new String[]{ String.valueOf(movieId) }
                    );
                    result = rowsDeleted > 0;
                    db.close();
                }
            }
        }

        return result;
    }
}
