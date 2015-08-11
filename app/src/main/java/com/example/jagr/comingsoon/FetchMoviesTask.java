package com.example.jagr.comingsoon;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.jagr.comingsoon.adapters.MoviesAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jagr on 7/30/2015.
 */
public class FetchMoviesTask extends AsyncTask<Void, Void, String> {
    private static final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    private Context mContext;
    private MoviesAdapter mAdapter;
    private boolean mFetching = false;

    public FetchMoviesTask(Context context, MoviesAdapter adapter) {
        mContext = context;
        mAdapter = adapter;
    }

    public boolean isFetching() {
        return mFetching;
    }

    @Override
    public void onPreExecute() {
        mFetching = true;
    }

    @Override
    public String doInBackground(Void... params) {
        String jsonResponse         = null;
        HttpURLConnection httpConn  = null;
        BufferedReader reader       = null;

        try {
            final String MOVIEDB_BASE_URL   = "http://api.themoviedb.org/3/discover/movie?";
            final String PAGE_PARAM         = "page";
            final String SORT_PARAM         = "sort_by";
            final String KEY_PARAM          = "api_key";

            Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                    .appendQueryParameter(PAGE_PARAM, String.valueOf(mAdapter.getCurrentPage() + 1))
                    .appendQueryParameter(SORT_PARAM, Utility.getPreferredSorting(mContext))
                    .appendQueryParameter(KEY_PARAM, Utility.getApiKey(mContext))
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

        return jsonResponse;
    }

    @Override
    public void onPostExecute(String jsonResponse) {
        if (jsonResponse != null) {
            try {
                JSONObject json     = new JSONObject(jsonResponse);
                JSONArray results   = json.optJSONArray("results");
                int page            = json.optInt("page");
                int totalPages      = json.optInt("total_pages");
                JSONArray data      = mAdapter.getData();

                // Filter out movies w/o a poster_path
                for (int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.optJSONObject(i);
                    if (!movie.isNull("poster_path") && !movie.optString("poster_path").equals("")) {
                        data.put(results.optJSONObject(i));
                    }
                }

                mAdapter.setCurrentPage(page);
                mAdapter.setPageCount(totalPages < 1000 ? totalPages : 1000);
                mAdapter.setData(data);
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.toString(), e);
                e.printStackTrace();
            }
        }
        mFetching = false;
    }
}