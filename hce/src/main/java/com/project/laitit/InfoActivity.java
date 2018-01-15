package com.project.laitit;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.project.laitit.util.AppPrefs;


public class InfoActivity extends AppCompatActivity {
    AppPrefs appPrefs;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        appPrefs = new AppPrefs(getApplicationContext());
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.app_name);
        }

        Drawable d = getResources().getDrawable(R.drawable.notification_bg);
        getSupportActionBar().setBackgroundDrawable(d);
    }
        //click on link
        public void linkClickEvent(View v) {
            try {
                switch (v.getId()) {
                    case R.id.about_app:
                        intent = new Intent(InfoActivity.this, AboutApp.class);
                        startActivity(intent);
                        break;
                    case R.id.about_event:
                        intent = new Intent(InfoActivity.this, AboutEvent.class);
                        startActivity(intent);
                        break;
                    case R.id.welcome_billing:
                        intent = new Intent(InfoActivity.this, WelcomeBilling.class);
                        startActivity(intent);
                        break;
                    case R.id.late_it_sponsors:
                        intent = new Intent(InfoActivity.this, LateItSponsors.class);
                        startActivity(intent);
                        break;
                    case R.id.hotel_layout:
                        intent = new Intent(InfoActivity.this, HotelLayout.class);
                        startActivity(intent);
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            //getFragmentManager().popBackStackImmediate();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
