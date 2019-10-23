package com.chromsicle.hackernews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ArticleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        WebView webView = findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        //makes sure the user's browser doesn't open
        webView.setWebViewClient(new WebViewClient());

        //get intent for the html and display
        Intent intent = getIntent();
        webView.loadData(intent.getStringExtra("content"), "text/html", "UTF-8");

    }
}
