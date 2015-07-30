package com.example.jagr.comingsoon;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.jagr.comingsoon.data.MoviesContract;
import com.example.jagr.comingsoon.data.MoviesDBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jagr on 7/24/2015.
 */
public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static String getApiKey(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_api_key_key), "");
    }

    public static boolean isApiKeySet(Context context) {
        String apiKey = getApiKey(context);
        return apiKey != null && apiKey.trim().length() > 0;
    }

    public static String getPreferredSorting(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_default));
    }

    public static String getDisplayDensity(Context context) {
        String result = "ldpi";
        float density = context.getResources().getDisplayMetrics().density;

        if (density >= 4.0) {
            // "w780"
            result = "xxxhdpi";
        } else if (density >= 3.0) {
            // "w500"
            result = "xxhdpi";
        } else if (density >= 2.0) {
            // "w342"
            result = "xhdpi";
        } else if (density >= 1.5) {
            // "w185"
            result = "hdpi";
        } else if (density >= 1.0) {
            // "w154"
            result = "mdpi";
        } else {
            // "w92"
            result = "ldpi";
        }

        return result;
    }
}
