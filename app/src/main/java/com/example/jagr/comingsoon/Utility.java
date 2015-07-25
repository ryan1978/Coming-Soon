package com.example.jagr.comingsoon;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by jagr on 7/24/2015.
 */
public class Utility {

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

}
