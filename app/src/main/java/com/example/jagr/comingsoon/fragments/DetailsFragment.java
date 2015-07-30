package com.example.jagr.comingsoon.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.jagr.comingsoon.R;
import com.example.jagr.comingsoon.Utility;
import com.example.jagr.comingsoon.activities.MainActivity;
import com.example.jagr.comingsoon.data.MoviesContract.MovieEntry;
import com.example.jagr.comingsoon.data.MoviesContract.VideoEntry;
import com.example.jagr.comingsoon.data.MoviesContract.ReviewEntry;
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

    private static final String STATE_VIDEO_DATA        = "video_data";
    private static final String STATE_REVIEW_PAGE       = "review_page";
    private static final String STATE_REVIEW_PAGE_COUNT = "review_page_count";
    private static final String STATE_REVIEW_DATA       = "review_data";

    private JSONObject mMovie;
    private VideoAdapter mVideoAdapter;
    private ReviewAdapter mReviewAdapter;

    public DetailsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startIntent = getActivity().getIntent();
        if (startIntent.hasExtra(MainActivity.MOVIE_EXTRA)) {
            try {
                mMovie = new JSONObject(startIntent.getStringExtra(MainActivity.MOVIE_EXTRA));
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.toString());
                e.printStackTrace();
            }
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_details, menu);

        if (Utility.isFavorite(getActivity(), mMovie)) {
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
            if (Utility.isFavorite(getActivity(), mMovie)) {
                if (Utility.removeFavorite(getActivity(), mMovie)) {
                    item.setIcon(R.drawable.ic_action_action_favorite_outline);
                }
            } else if (Utility.addFavorite(getActivity(), mMovie)) {
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
        outState.putInt(STATE_REVIEW_PAGE, mReviewAdapter.getCurrentPage());
        outState.putInt(STATE_REVIEW_PAGE_COUNT, mReviewAdapter.getPageCount());
        outState.putString(STATE_REVIEW_DATA, mReviewAdapter.getData().toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        if (mMovie != null) {
            mVideoAdapter = new VideoAdapter(getActivity(), mMovie.optInt("id"));
            mReviewAdapter = new ReviewAdapter(getActivity(), mMovie.optInt("id"));
            if (savedInstanceState != null) {
                mVideoAdapter.setData(savedInstanceState.getString(STATE_VIDEO_DATA));
                mReviewAdapter.setData(savedInstanceState.getInt(STATE_REVIEW_PAGE),
                        savedInstanceState.getInt(STATE_REVIEW_PAGE_COUNT),
                        savedInstanceState.getString(STATE_REVIEW_DATA));
            } else {
                mVideoAdapter.fetch();
                mReviewAdapter.fetch();
            }

            final ScrollView details    = (ScrollView) rootView.findViewById(R.id.details_scrollview);
            final ImageView backdrop    = (ImageView) rootView.findViewById(R.id.backdrop_image);
            final ImageView poster      = (ImageView) rootView.findViewById(R.id.poster_image);
            final TextView title        = (TextView) rootView.findViewById(R.id.original_title);
            final TextView overview     = (TextView) rootView.findViewById(R.id.overview);
            final TextView rating       = (TextView) rootView.findViewById(R.id.vote_average);
            final TextView release      = (TextView) rootView.findViewById(R.id.release_date);
            final ListView videos       = (ListView) rootView.findViewById(R.id.video_list);
            final ListView reviews      = (ListView) rootView.findViewById(R.id.review_list);

            videos.setAdapter(mVideoAdapter);
            // As the list items are added to the video list this keeps the parent scrollview
            // scrolled all the way to the top
            videos.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    details.fullScroll(View.FOCUS_UP);
                }
            });
            // On click of video list item open up video in YouTube app
            videos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent videoPlay = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + ((JSONObject) mVideoAdapter.getItem(position)).optString(VideoEntry.COLUMN_KEY)));
                    getActivity().startActivity(videoPlay);
                }
            });

            reviews.setAdapter(mReviewAdapter);
            // As the list items are added to the video list this keeps the parent scrollview
            // scrolled all the way to the top
            reviews.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    details.fullScroll(View.FOCUS_UP);
                }
            });

            if (!mMovie.isNull(MovieEntry.COLUMN_BACKDROP_PATH) && !mMovie.optString(MovieEntry.COLUMN_BACKDROP_PATH).equals("")) {
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w780" + mMovie.optString(MovieEntry.COLUMN_BACKDROP_PATH)).into(backdrop);
            } else if (!mMovie.isNull(MovieEntry.COLUMN_POSTER_PATH) && !mMovie.optString(MovieEntry.COLUMN_POSTER_PATH).equals("")) {
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w780" + mMovie.optString(MovieEntry.COLUMN_POSTER_PATH)).into(backdrop);
            } else {
                // TODO: FIND PLACEHOLDER IMAGE FOR BACKDROP
            }
            if (!mMovie.isNull(MovieEntry.COLUMN_POSTER_PATH) && !mMovie.optString(MovieEntry.COLUMN_POSTER_PATH).equals("")) {
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w342" + mMovie.optString(MovieEntry.COLUMN_POSTER_PATH)).into(poster);
            } else {
                // TODO: FIND PLACEHOLDER IMAGE FOR POSTER
            }
            title.setText(mMovie.isNull(MovieEntry.COLUMN_TITLE) ? "Not Available" : mMovie.optString(MovieEntry.COLUMN_TITLE));
            overview.setText(mMovie.isNull(MovieEntry.COLUMN_OVERVIEW) ? "Not Available" : mMovie.optString(MovieEntry.COLUMN_OVERVIEW));
            rating.setText("Rating: " + (mMovie.isNull(MovieEntry.COLUMN_VOTE_AVERAGE) ? "Not Available" : mMovie.optString(MovieEntry.COLUMN_VOTE_AVERAGE) + "/10"));
            release.setText("Released: " + (mMovie.isNull(MovieEntry.COLUMN_RELEASE_DATE) ? "Not Available" : mMovie.optString(MovieEntry.COLUMN_RELEASE_DATE)));
        }

        return rootView;
    }

    /**
     * Custom adapter class to populate videos list view
     */
    private static class VideoAdapter extends BaseAdapter {
        private static final String LOG_TAG = VideoAdapter.class.getSimpleName();
        private final WeakReference<Activity> mActivity;
        private int mMovieId;
        private JSONArray mVideos   = new JSONArray();
        private boolean mFetching   = false;

        public VideoAdapter(Activity activity, int movieId) {
            mActivity   = new WeakReference<>(activity);
            mMovieId    = movieId;
        }

        @Override
        public int getCount() {
            return mVideos.length();
        }

        @Override
        public Object getItem(int position) {
            return mVideos.optJSONObject(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            JSONObject video    = mVideos.optJSONObject(position);
            Activity activity   = mActivity.get();

            if (activity != null) {
                if (convertView == null) {
                    LayoutInflater vi = LayoutInflater.from(activity);
                    convertView = vi.inflate(R.layout.list_item_video, parent, false);
                }

                TextView name = (TextView) convertView.findViewById(R.id.title_text);
                name.setText(video.optString(VideoEntry.COLUMN_NAME));
            }

            return convertView;
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
         * Resets the adapter and empties ListView
         */
        public void clearData() {
            mVideos = new JSONArray();
            notifyDataSetChanged();
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
            if (!mFetching) {
                FetchVideosTask fetcher = new FetchVideosTask(VideoAdapter.this);
                fetcher.execute();
            }
        }

        private static class FetchVideosTask extends AsyncTask<Void, Void, String> {
            private static final String LOG_TAG = FetchVideosTask.class.getSimpleName();
            private final WeakReference<VideoAdapter> mAdapter;

            FetchVideosTask(VideoAdapter adapter) {
                mAdapter = new WeakReference<>(adapter);
            }

            @Override
            public void onPreExecute() {
                VideoAdapter adapter = mAdapter.get();
                if (adapter != null) {
                    adapter.mFetching = true;
                }
            }

            @Override
            public String doInBackground(Void... params) {
                String jsonResponse         = null;
                final VideoAdapter adapter  = mAdapter.get();

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
                VideoAdapter adapter = mAdapter.get();

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

                adapter.mFetching = false;
            }
        }
    }

    /**
     * Custom adapter class to populate reviews list view
     */
    private static class ReviewAdapter extends BaseAdapter {
        private static final String LOG_TAG = ReviewAdapter.class.getSimpleName();
        private final WeakReference<Activity> mActivity;
        private int mMovieId;
        private int mCurrentPage    = 0;
        private int mTotalPages     = 0;
        private JSONArray mReviews  = new JSONArray();
        private boolean mFetching   = false;

        public ReviewAdapter(Activity activity, int movieId) {
            mActivity   = new WeakReference<Activity>(activity);
            mMovieId    = movieId;
        }

        @Override
        public int getCount() {
            return mReviews.length();
        }

        @Override
        public Object getItem(int position) {
            return mReviews.optJSONObject(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            JSONObject review       = mReviews.optJSONObject(position);
            Activity activity       = mActivity.get();

            if (activity != null) {
                if (convertView == null) {
                    LayoutInflater vi = LayoutInflater.from(activity);
                    convertView = vi.inflate(R.layout.list_item_review, parent, false);
                }

                TextView reviewer   = (TextView) convertView.findViewById(R.id.reviewer_name);
                TextView content    = (TextView) convertView.findViewById(R.id.review_text);
                reviewer.setText(review.optString(ReviewEntry.COLUMN_AUTHOR));
                content.setText(review.optString(ReviewEntry.COLUMN_CONTENT));
            }

            return convertView;
        }

        // Getters used for saving state
        public JSONArray getData()  { return mReviews; }
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
                mReviews         = new JSONArray(data);
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
            mReviews         = new JSONArray();
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
            if (!mFetching) {
                FetchReviewsTask fetcher = new FetchReviewsTask(ReviewAdapter.this);
                fetcher.execute();
            }
        }

        private static class FetchReviewsTask extends AsyncTask<Void, Void, String> {
            private static final String LOG_TAG = FetchReviewsTask.class.getSimpleName();
            private final WeakReference<ReviewAdapter> mAdapter;

            FetchReviewsTask(ReviewAdapter adapter) {
                mAdapter = new WeakReference<>(adapter);
            }

            @Override
            public void onPreExecute() {
                ReviewAdapter adapter = mAdapter.get();
                if (adapter != null) {
                    adapter.mFetching = true;
                }
            }

            @Override
            public String doInBackground(Void... params) {
                String jsonResponse         = null;
                final ReviewAdapter adapter  = mAdapter.get();

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
                ReviewAdapter adapter = mAdapter.get();

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
                                adapter.mReviews.put(results.optJSONObject(i));
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
