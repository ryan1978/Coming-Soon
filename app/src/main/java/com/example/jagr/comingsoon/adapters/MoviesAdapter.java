package com.example.jagr.comingsoon.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.jagr.comingsoon.R;
import com.example.jagr.comingsoon.Utility;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jagr on 7/30/2015.
 */
public class MoviesAdapter extends JSONArrayAdapter {

    private static final String LOG_TAG = MoviesAdapter.class.getSimpleName();

    private Context mContext;
    private int mCurrentPage    = 0;
    private int mPageCount      = 0;

    public MoviesAdapter(Context context) {
        mContext = context;
    }

    /**
     * Set the current page of movies
     * @param page The current page number
     */
    public void setCurrentPage(int page) {
        mCurrentPage = page;
    }
    public int getCurrentPage() { return mCurrentPage; }
    /**
     * Set the total number of pages that can be retrieved
     * @param count The total number of pages that can be retrieved
     */
    public void setPageCount(int count) {
        mPageCount = count;
    }
    public int getPageCount() {
        return mPageCount;
    }

    /**
     * Returns a boolean indicating if more pages can be fetched
     * @return boolean indicating if more pages can be fetched
     */
    public boolean hasMorePages() {
        return mCurrentPage < mPageCount;
    }

    @Override
    public void empty() {
        mCurrentPage = 0;
        mPageCount = 0;
        super.empty();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JSONObject movie        = mData.optJSONObject(position);
        String posterPath       = movie.optString("poster_path");

        if (convertView == null) {
            LayoutInflater vi = LayoutInflater.from(mContext);
            convertView = vi.inflate(R.layout.grid_item_movie, parent, false);
        }

        ImageView poster = (ImageView) convertView.findViewById(R.id.poster_image);

        if (posterPath.trim().length() > 0) {
            Picasso
                    .with(mContext)
                    .load("http://image.tmdb.org/t/p/" + Utility.getImageFolder(mContext) + posterPath)
                    .into(poster);
        }

        return convertView;
    }
}
