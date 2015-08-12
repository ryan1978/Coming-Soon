package com.example.jagr.comingsoon.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.jagr.comingsoon.R;
import com.example.jagr.comingsoon.Utility;
import com.example.jagr.comingsoon.fragments.DetailsFragment;
import com.example.jagr.comingsoon.fragments.MovieGridFragment;

import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener, MovieGridFragment.Callback {

    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    private final static String STATE_FILTER_DIALOG = "filter_dialog";
    private final static String DETAILFRAGMENT_TAG = "DFTAG";

    /** Alert Dialog prompting for API Key */
    private AlertDialog mAPIKeyAlert;
    private AlertDialog mFilterDialog;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }

        // Register this activity as a listener for shared pref changes
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.registerOnSharedPreferenceChangeListener(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(STATE_FILTER_DIALOG)) {
                showFilterAlert();
            }
        }
    }

    @Override
    public void onStart() {
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
        } else if (id == R.id.action_filter) {
            showFilterAlert();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mFilterDialog != null && mFilterDialog.isShowing()) {
            outState.putBoolean(STATE_FILTER_DIALOG, true);
        } else {
            outState.putBoolean(STATE_FILTER_DIALOG, false);
        }
        super.onSaveInstanceState(outState);
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
                        finish();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
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
     * Shows an alert dialog to select a movie filter
     */
    private void showFilterAlert() {
        final String[] entries = getResources().getStringArray(R.array.pref_sort_entries);
        final String[] values = getResources().getStringArray(R.array.pref_sort_values);

        mFilterDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_filter)
                .setSingleChoiceItems(entries,
                        Arrays.asList(values).indexOf(Utility.getPreferredSorting(this)),
                        new DialogInterface.OnClickListener() {
                            @Override
                                public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(getResources().getString(R.string.pref_sort_key), values[which]);
                                editor.apply();
                            }
                        }
                ).create();

        mFilterDialog.show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        // If the user changes the api key or sort settings attempt to refetch movies
        if (key.equals(getString(R.string.pref_api_key_key))
                || key.equals(getString(R.string.pref_sort_key))) {
            if (Utility.isApiKeySet(this)) {
                if (mAPIKeyAlert != null && mAPIKeyAlert.isShowing()) {
                    mAPIKeyAlert.dismiss();
                }

                // Reset the adapter to requery the MoveDbApi
                MovieGridFragment mgf = (MovieGridFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_movie_grid);
                if (mgf != null) {
                    mgf.resetAdapter();
                }
            }

            // Remove the details fragment is one exists
            DetailsFragment df = (DetailsFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (df != null) {
                getSupportFragmentManager().beginTransaction().remove(df).commitAllowingStateLoss();
            }
        }
    }

    @Override
    public void onMovieSelected(JSONObject movie) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putString(DetailsFragment.MOVIE_EXTRA, movie.toString());

            DetailsFragment fragment = new DetailsFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(DetailsFragment.MOVIE_EXTRA, movie.toString());
            startActivity(intent);
        }
    }
}
