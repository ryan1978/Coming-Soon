package com.example.jagr.comingsoon.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.jagr.comingsoon.R;
import com.example.jagr.comingsoon.Utility;
import com.example.jagr.comingsoon.adapters.MoviesAdapter;

import org.json.JSONObject;

import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends Fragment {

    private static final String LOG_TAG             = MovieGridFragment.class.getSimpleName();
    private static final String STATE_SELECTION     = "selection";
    private static final String STATE_PAGE          = "page";
    private static final String STATE_PAGE_COUNT    = "page_count";
    private static final String STATE_DATA          = "data";

    private MoviesAdapter mAdapter;
    private GridView mMoviesGrid;

    public interface Callback {
        public void onMovieSelected(JSONObject movie);
    }

    public MovieGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        mAdapter = new MoviesAdapter(getActivity());

        if (savedInstanceState != null) {
            mAdapter.setCurrentPage(savedInstanceState.getInt(STATE_PAGE));
            mAdapter.setPageCount(savedInstanceState.getInt(STATE_PAGE_COUNT));
            mAdapter.setData(savedInstanceState.getString(STATE_DATA));
            mMoviesGrid.setSelection(savedInstanceState.getInt(STATE_SELECTION));
        } else if (Utility.isApiKeySet(getActivity())) {
            mAdapter.fetchMovies();
        }

        mMoviesGrid.setAdapter(mAdapter);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMoviesGrid.setOnScrollListener(new MovieScrollWatcher());
        mMoviesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "onItemClick");
                ((Callback) getActivity()).onMovieSelected((JSONObject) mAdapter.getItem(position));
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mMoviesGrid.setOnScrollListener(null);
        mMoviesGrid.setOnItemClickListener(null);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);

        mMoviesGrid = (GridView) rootView.findViewById(R.id.movies_grid);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTION, mMoviesGrid.getFirstVisiblePosition());
        outState.putInt(STATE_PAGE, mAdapter.getCurrentPage());
        outState.putInt(STATE_PAGE_COUNT, mAdapter.getPageCount());
        outState.putString(STATE_DATA, mAdapter.getData().toString());
        super.onSaveInstanceState(outState);
    }

    /**
     * Class to watch for the GridView being scrolled to the bottom that will
     * fetch and append additional results
     */
    private class MovieScrollWatcher implements GridView.OnScrollListener {
        private boolean mUserScrolled = false;

        @Override
        public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                mUserScrolled = true;
            }
        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            // As the grid is scrolled towards the bottom fetch more movies
            if (mUserScrolled && mAdapter.hasMorePages()) {
                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem >= mAdapter.getCount() - 6) {
                    mAdapter.fetchMovies();
                }
            }
        }
    }

    /**
     * Empties the gridview and requeries MovieDbApi to fill it again
     */
    public void resetAdapter() {
        mMoviesGrid.smoothScrollToPosition(0);
        mAdapter.empty();
        mAdapter.fetchMovies();
    }
}
