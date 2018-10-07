package com.sommerengineering.news;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Article>> {

    // simple string tag for log messages
    public static final String LOG_TAG = MainActivity.class.getName();

    // URL query returns JSON object representing news articles from The Guardian
    // query is "mexico surf" returns 20 most recent articles
    private static final String GUARDIAN_REQUEST_URL =
            "https://content.guardianapis.com/search?order-by=newest&q=Mexico%20and%20surf&page-size=20&show-tags=contributor&show-elements=image&show-fields=headline,thumbnail,trailText&api-key=d34b30e0-7d4c-42c9-9bc4-0af20234ffc4";

    // constant value for the ID of the single article loader
    private static final int ARTICLE_LOADER_ID = 0;

    // define state variables to be initialized in onCreate()
    private ArticleAdapter mAdapter;
    private TextView mEmptyTextView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // super class constructor
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize an empty ArrayList to hold Earthquake objects
        ArrayList<Article> articles = new ArrayList<>();

        // find a reference to the ListView
        ListView articleListView = (ListView) findViewById(R.id.list);

        // custom adapter populates ListView
        mAdapter = new ArticleAdapter(this, articles);
        articleListView.setAdapter(mAdapter);

        // define an empty view in the rare case no articles exist for the URL query parameters
        mEmptyTextView = (TextView) findViewById(R.id.empty_list);
        articleListView.setEmptyView(mEmptyTextView);

        // define and display ProgressBar
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // set an item click listener on the ListView items
        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            // click opens up article source page for more detailed information
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // get current Earthquake object
                Article currentArticle = mAdapter.getItem(position);

                // Uri object to pass into web browser Intent
                String url = currentArticle.getUrl();
                Uri articleUri = Uri.parse(url);

                // create Intent to open web browser
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, articleUri);

                // this conditional prevents the app from crashing by ensuring
                // a web browser actually exists on the phone
                if (websiteIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(websiteIntent);
                }

            }

        });

        // check status for internet connectivity
        if (isConnected()) {

            // initialize a loader manager to handle a background thread
            LoaderManager loaderManager = getLoaderManager();

            // automatically calls onCreateLoader()
            loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);

        }
        else { // not connected to internet

            // hide the progress bar
            mProgressBar.setVisibility(View.GONE);

            // the articles list is empty
            mEmptyTextView.setText(R.string.no_internet_connection);

        }

    }

    // automatically called when the loader manager determines that a loader with an id of
    // ARTICLE_LOADER_ID does not exist
    @Override
    public Loader<List<Article>> onCreateLoader(int i, Bundle bundle) {

        // create and return a new loader with the given URL
        ArticleLoader loader = new ArticleLoader(this, GUARDIAN_REQUEST_URL);
        return loader;

    }

    // automatically called when loader background thread completes
    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {

        // clear the adapter of any previous query to USGS database
        mAdapter.clear();

        // hide the progress bar
        mProgressBar.setVisibility(View.GONE);

        // check the input exists and is not empty
        if (articles != null && !articles.isEmpty()) {

            // calling addAll method on the adapter automatically triggers the ListView to update
            mAdapter.addAll(articles);
        }
        else {

            // this conditional handles the rare edge case of (1) successful network call and population of ListView
            // (2) leave app, (3) lose internet connection, (4) return to app
            if (isConnected()) {

                // the articles list is empty because there are no articles matching the search criteria
                mEmptyTextView.setText(R.string.no_articles_found);

            }

            // internet connection was lost after a loader with ARTICLE_LOADER_ID was successfully completed
            else {

                // the articles list is empty because there is no internet connection
                mEmptyTextView.setText(R.string.no_internet_connection);
            }

        }

    }

    // previously created loader is no longer needed and existing data should be discarded
    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {

        // removing all data from adapter automatically clears the UI listview
        mAdapter.clear();

    }

    // check status of internet connectivity
    public boolean isConnected() {

        // get connectivity status as a boolean
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();

        return isConnected;

    }

}
