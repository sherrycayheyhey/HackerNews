package com.chromsicle.hackernews;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> titles = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create a new download task
        DownloadTask task = new DownloadTask();
        try{
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //get access to the ListView and set up the ArrayAdapter
        ListView newsList = findViewById(R.id.newsListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);
        newsList.setAdapter(arrayAdapter);
    }

    //create a task so we can go get the appropriate data from the API
    //pass in a string, void for the updates, return a string
    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            // try/catch to get something from the URL and get an input stream
            try {
                //take the URL and try to convert it to a string
                url = new URL(urls[0]);
                //set up the URL connection
                urlConnection = (HttpURLConnection) url.openConnection();
                //create an input stream
                InputStream inputStream = urlConnection.getInputStream();
                //get an input string reader
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                //parse through each bit of data that we need
                int data = inputStreamReader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }
                Log.i("URL CONTENT", result);
                return result;


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
