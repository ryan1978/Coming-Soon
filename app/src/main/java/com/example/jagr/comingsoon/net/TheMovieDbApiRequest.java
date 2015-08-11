package com.example.jagr.comingsoon.net;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.jagr.comingsoon.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ryan Gilreath on 7/31/2015.
 */
public class TheMovieDbApiRequest {

    private static final String LOG_TAG             = TheMovieDbApiRequest.class.getSimpleName();

    private static final String MOVIEDB_BASE_URL    = "http://api.themoviedb.org/3/";
    public enum ApiCall {
        DISCOVER_MOVIES("discover/movie"),
        NOWPLAYING_MOVIES("movie/now_playing"),
        POPULAR_MOVIES("movie/popular"),
        TOPRATED_MOVIES("movie/top_rated"),
        UPCOMING_MOVIES("movie/upcoming"),
        MOVIE_VIDEOS("movie/{0}/videos"),
        MOVIE_REVIEWS("movie/{0}/reviews");

        private String mPath;
        ApiCall(String path) {
            mPath = path;
        }
        public String getPath() { return mPath; }
    }

    private Context mContext;
    private ApiCall mEndPoint;
    private String[] mPathParams;

    public TheMovieDbApiRequest(Context context, ApiCall endPoint, String[] pathParams) {
        mContext    = context;
        mEndPoint   = endPoint;
        mPathParams = pathParams;
    }

    public JSONObject doAPIRequest(HashMap<String, String> queryParams) {

        HttpURLConnection httpConn  = null;
        BufferedReader reader       = null;
        String jsonResponse         = "{}";

        try {
            URL url = new URL(buildUrl(queryParams));

            Log.d(LOG_TAG, url.toString());

            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            InputStream inputStream = httpConn.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() > 0) {
                    jsonResponse = buffer.toString();
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString(), e);
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

        JSONObject apiResult = null;
        try {
            apiResult = new JSONObject(jsonResponse);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing data " + jsonResponse, e);
        }

        return apiResult;
    }

    private String buildUrl(HashMap<String, String> queryParams) {
        String baseUri = MOVIEDB_BASE_URL + mEndPoint.getPath() + "?";

        // Replace any path params
        if (mPathParams != null) {
            for (int i = 0; i < mPathParams.length; i++) {
                baseUri = baseUri.replace("{" + i + "}", mPathParams[i]);
            }
        }

        Uri uri = Uri.parse(baseUri);

        // Append any query parameters to URI
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                uri = uri.buildUpon().appendQueryParameter(key, value).build();
            }
        }

        // Append API Key
        uri = uri.buildUpon().appendQueryParameter("api_key", Utility.getApiKey(mContext)).build();

        return uri.toString();
    }
}
