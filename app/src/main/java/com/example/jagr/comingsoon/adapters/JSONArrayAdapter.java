package com.example.jagr.comingsoon.adapters;

import android.content.Context;
import android.util.Log;
import android.widget.BaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by jagr on 7/30/2015.
 */
public abstract class JSONArrayAdapter extends BaseAdapter {

    private static final String LOG_TAG = JSONArrayAdapter.class.getSimpleName();

    protected JSONArray mData = new JSONArray();

    /**
     * Sets the JSONArray data backing this adapter
     * @param data The JSONArray of data this adapter uses
     */
    public void setData(JSONArray data) {
        mData = data;
    }
    /**
     * Sets the JSONArray data backing this adapter
     * @param data The JSONArray of data this adapter uses
     */
    public void setData(String data) {
        try {
            mData = new JSONArray(data);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
            e.printStackTrace();
        }
    }
    public JSONArray getData() {
        return mData;
    }

    /**
     * Empties the data from this adapter
     */
    protected void empty() {
        mData = new JSONArray();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.length();
    }

    @Override
    public Object getItem(int position) {
        return mData.opt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
