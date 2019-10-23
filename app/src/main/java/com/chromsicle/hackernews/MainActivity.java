package com.chromsicle.hackernews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> titles = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    ArrayList<String> content = new ArrayList<>();

    //make a database to store the info
    SQLiteDatabase articlesDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get the database ready
        articlesDB = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);
        //set up the table
        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId, INTEGER, title VARCHAR, content VARCHAR)");

        //create a new download task
        DownloadTask task = new DownloadTask();
        try{
            //uncomment this if you have lots of time to download the articles or if it's the first time running 
            //task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //get access to the ListView and set up the ArrayAdapter
        ListView newsList = findViewById(R.id.newsListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);
        newsList.setAdapter(arrayAdapter);

        //intent for sending to the ArticleActivity
        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                intent.putExtra("content", content.get(i));

                startActivity(intent);
            }
        });

        updateListView();
    }

    //update the app when there's new info
    public void updateListView() {
        //get stuff out of the database, get it into the appropriate arrays, then display that
        Cursor c = articlesDB.rawQuery("SELECT * FROM articles", null);
        //get the content and title
        int contentIndex = c.getColumnIndex("content");
        int titleIndex = c.getColumnIndex("title");

        if(c.moveToFirst()) {
            titles.clear();
            content.clear();

            do {
                titles.add(c.getString(titleIndex));
                content.add(c.getString(contentIndex));

            } while (c.moveToNext());

            arrayAdapter.notifyDataSetChanged();
        }

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

                //take the string that is an array but turn it into a JSON array so we can loop from stuff
                JSONArray jsonArray = new JSONArray(result);

                int numberOfItems = 3;

                //if there's less than 20 articles (probably won't happen though), set the number accordingly
                if (jsonArray.length() < numberOfItems){
                    numberOfItems = jsonArray.length();
                }

                //clear out the table before hitting up all the info
                articlesDB.execSQL("DELETE FROM articles");

                for (int i = 0; i < numberOfItems; i++) {
                    //get the article ID
                    String articleID = jsonArray.getString(i);
                    //get the URL for that ID
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleID + ".json?print=pretty");

                    //set up the URL connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    //create an input stream
                    inputStream = urlConnection.getInputStream();
                    //get an input string reader
                    inputStreamReader = new InputStreamReader(inputStream);
                    //parse through each bit of data that we need
                    data = inputStreamReader.read();
                    String articleInfo = "";

                    while (data != -1) {
                        char current = (char) data;
                        articleInfo += current;
                        data = inputStreamReader.read();
                    }

                    //parse through the info to get the title and the url
                    JSONObject jsonObject = new JSONObject(articleInfo);
                    if (!jsonObject.isNull("title") && !jsonObject.isNull("url")) {
                        //there's a title and a url to work with so let's get them!
                        String articleTitle = jsonObject.getString("title");
                        String articleUrl = jsonObject.getString("url");

                        //take the title and the URL and download the appropriate info
                        //pass the URL into our URL object
                        url = new URL(articleUrl);
                        //re-setup the url connection
                        urlConnection = (HttpURLConnection) url.openConnection();
                        //get the input stream
                        inputStream = urlConnection.getInputStream();
                        //set up the input stream reader
                        inputStreamReader = new InputStreamReader(inputStream);
                        //get the data set up
                        data = inputStreamReader.read();
                        //a string to represent the HTML from the article
                        String articleContent = "";

                        //get all the data
                        while (data != -1) {
                            char current = (char) data;
                            articleContent += current;
                            data = inputStreamReader.read();
                        }
                        Log.i("HTML", articleContent);

                        //make a string of the sql content
                        String sql = "INSERT INTO articles(articleID, title, content) VALUES (?, ?, ?)";
                        SQLiteStatement statement = articlesDB.compileStatement(sql);
                        statement.bindString(1, articleID);
                        statement.bindString(2, articleTitle);
                        statement.bindString(3, articleContent);

                        statement.execute();

                    }
                }

                Log.i("URL CONTENT", result);
                return result;


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            updateListView();
        }
    }
}
