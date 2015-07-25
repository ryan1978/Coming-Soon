package com.example.jagr.comingsoon.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jagr.comingsoon.R;
import com.example.jagr.comingsoon.activities.MainActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragment extends Fragment {

    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();

    private static final String KEY_BACKDROP    = "backdrop_path";
    private static final String KEY_POSTER      = "poster_path";
    private static final String KEY_TITLE       = "original_title";
    private static final String KEY_OVERVIEW    = "overview";
    private static final String KEY_RATING      = "vote_average";
    private static final String KEY_RELEASE     = "release_date";

    private JSONObject mMovie;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        if (mMovie != null) {
            final ImageView backdrop    = (ImageView) rootView.findViewById(R.id.backdrop_image);
            final ImageView poster      = (ImageView) rootView.findViewById(R.id.poster_image);
            final TextView title        = (TextView) rootView.findViewById(R.id.original_title);
            final TextView overview     = (TextView) rootView.findViewById(R.id.overview);
            final TextView rating       = (TextView) rootView.findViewById(R.id.vote_average);
            final TextView release      = (TextView) rootView.findViewById(R.id.release_date);

            if (!mMovie.isNull(KEY_BACKDROP) && !mMovie.optString(KEY_BACKDROP).equals("")) {
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w780" + mMovie.optString(KEY_BACKDROP)).into(backdrop);
            } else if (!mMovie.isNull(KEY_POSTER) && !mMovie.optString(KEY_POSTER).equals("")) {
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w780" + mMovie.optString(KEY_POSTER)).into(backdrop);
            } else {
                // TODO: FIND PLACEHOLDER IMAGE FOR BACKDROP
            }
            if (!mMovie.isNull(KEY_POSTER) && !mMovie.optString(KEY_POSTER).equals("")) {
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w342" + mMovie.optString(KEY_POSTER)).into(poster);
            } else {
                // TODO: FIND PLACEHOLDER IMAGE FOR POSTER
            }
            title.setText(mMovie.isNull(KEY_TITLE) ? "Not Available" : mMovie.optString(KEY_TITLE));
            overview.setText(mMovie.isNull(KEY_OVERVIEW) ? "Not Available" : mMovie.optString(KEY_OVERVIEW));
            rating.setText("Rating: " + (mMovie.isNull(KEY_RATING) ? "Not Available" : mMovie.optString(KEY_RATING) + "/10"));
            release.setText("Released: " + (mMovie.isNull(KEY_RELEASE) ? "Not Available" : mMovie.optString(KEY_RELEASE)));
        }

        return rootView;
    }
}
