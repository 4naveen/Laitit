package com.project.laitit;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.project.laitit.util.AppPrefs;
import com.project.laitit.util.Config;

public class LateItSponsors extends AppCompatActivity {

    AppPrefs appPrefs;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_late_it_sponsors);
        appPrefs = new AppPrefs(getApplicationContext());
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("LaitItSponsors");
        }
        Drawable d = getResources().getDrawable(R.drawable.notification_bg);
        getSupportActionBar().setBackgroundDrawable(d);

        WebView myWebView = (WebView) findViewById(R.id.webview_sponsors);
        myWebView.loadUrl(Config.latetitSponsors);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
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
