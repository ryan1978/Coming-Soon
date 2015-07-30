package com.example.jagr.comingsoon;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.jagr.comingsoon.data.MoviesContract;

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

    public static JSONArray getFavorites(Context context) {
        JSONArray result = new JSONArray();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favs = prefs.getStringSet(context.getString(R.string.pref_favorites), new HashSet<String>());
        for (String fav : favs) {
            try {
                result.put(new JSONObject(fav));
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.toString());
                e.printStackTrace();
            }
        }
        return result;
    }

    public static boolean addFavorite(Context context, JSONObject movie) {
        boolean result = false;

        if (movie != null) {
            if (!isFavorite(context, movie)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                Set<String> favs = prefs.getStringSet(context.getString(R.string.pref_favorites), new HashSet<String>());
                if (favs.add(movie.toString())) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putStringSet(context.getString(R.string.pref_favorites), favs);
                    result = editor.commit();
                }
            }
        }

        return result;
    }

    public static boolean removeFavorite(Context context, JSONObject movie) {
        boolean result = false;

        if (movie != null) {
            if (isFavorite(context, movie)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                Set<String> favs = prefs.getStringSet(context.getString(R.string.pref_favorites), new HashSet<String>());
                if (favs.remove(movie.toString())) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putStringSet(context.getString(R.string.pref_favorites), favs);
                    result = editor.commit();
                }
            }
        }

        return result;
    }

    public static boolean isFavorite(Context context, JSONObject movie) {
        boolean result = false;

        if (movie != null) {
            final int movieId = movie.optInt("id");

            if (movieId > 0) {
                JSONArray favs = getFavorites(context);
                for (int i = 0; i < favs.length(); i++) {
                    JSONObject fav = favs.optJSONObject(i);
                    if (fav.optInt("id") == movieId) {
                        result = true;
                        break;
                    }
                }
            }
        }

        return result;
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
