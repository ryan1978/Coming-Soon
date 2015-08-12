package com.example.jagr.comingsoon;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    public static String getImageFolder(Context context) {
        String result = "ldpi";
        float density = context.getResources().getDisplayMetrics().density;

        if (density >= 4.0) {
            // "xxxhdpi"
            result = "w780";
        } else if (density >= 3.0) {
            // "xxhdpi"
            result = "w500";
        } else if (density >= 2.0) {
            // "xhdpi"
            result = "w342";
        } else if (density >= 1.5) {
            // "hdpi"
            result = "w185";
        } else if (density >= 1.0) {
            // "mdpi"
            result = "w154";
        } else {
            // "ldpi"
            result = "w92";
        }

        return result;
    }

    public static int getImagePlaceholderId(Context context) {
        int placeHolderId;
        String imageFolder = getImageFolder(context);

        if (imageFolder.equals("w92")) {
            placeHolderId = R.drawable.placeholder_w92;
        } else if (imageFolder.equals("w154")) {
            placeHolderId = R.drawable.placeholder_w154;
        } else if (imageFolder.equals("w185")) {
            placeHolderId = R.drawable.placeholder_w185;
        } else if (imageFolder.equals("w342")) {
            placeHolderId = R.drawable.placeholder_w342;
        } else if (imageFolder.equals("w500")) {
            placeHolderId = R.drawable.placeholder_w500;
        } else {
            placeHolderId = R.drawable.placeholder_w780;
        }

        return placeHolderId;
    }
}
