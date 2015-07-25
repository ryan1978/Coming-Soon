package com.example.jagr.comingsoon.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.jagr.comingsoon.R;
import com.example.jagr.comingsoon.fragments.DetailsFragment;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new DetailsFragment())
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.slide_in_ltr, R.anim.slide_out_ltr);
    }
}
