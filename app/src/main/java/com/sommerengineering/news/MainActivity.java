package com.sommerengineering.news;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Article>> {

    // simple string tag for log messages
    public static final String LOG_TAG = MainActivity.class.getName();

    // URL query returns JSON object representing news articles from The Guardian
    private static final String GUARDIAN_REQUEST_URL =
            "https://content.guardianapis.com/search";

    // "https://content.guardianapis.com/search?order-by=newest&q=Mexico%20and%20surf&page-size=20&show-tags=contributor&show-elements=image&show-fields=headline,thumbnail,trailText&api-key=d34b30e0-7d4c-42c9-9bc4-0af20234ffc4"
    // ?order-by=newest&q=Mexico%20and%20surf&page-size=20&show-tags=contributor&show-elements=image&show-fields=headline,thumbnail,trailText&api-key=d34b30e0-7d4c-42c9-9bc4-0af20234ffc4

    // "https://earthquake.usgs.gov/fdsnws/event/1/query"
    // "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&limit=10&minmag=minMagnitude&orderby=orderBy"

    // constant value for the ID of the single article loader
    private static final int ARTICLE_LOADER_ID = 0;

    // define state variables to be initialized in onCreate()
    private ArticleAdapter mAdapter;
    private TextView mEmptyTextView;
    private ProgressBar mProgressBar;

    // initialize options menu in Action Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // inflate menu
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // called when the settings menu is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // get id of menu item
        int id = item.getItemId();

        // hamburger icon in top-right is pressed
        if (id == R.id.action_settings) {

            // explicit Intent to start new SettingsActivity
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        // call through to base class to perform the default menu handling
        return super.onOptionsItemSelected(item);
    }

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

        // get the hardcoded default preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // retrieve user preference for minimum magnitude
        // a reference to the default preference is required by getString
        String pageCountKey = getString(R.string.settings_min_magnitude_key);
        String pageCountDefaultValue = getString(R.string.settings_min_magnitude_default);
        String pageCount = sharedPrefs.getString(pageCountKey, pageCountDefaultValue);

        // retrieve user preference for order-by
        // a reference to the default preference is required by getString
        String orderByKey = getString(R.string.settings_order_by_key);
        String orderByDefaultValue = getString(R.string.settings_order_by_default);
        String orderBy = sharedPrefs.getString(orderByKey, orderByDefaultValue);

        // split URL String into constituent parts
        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);

        // prepare URI object for appending query parameters
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // append query parameters, for example "format=geojson"
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("q", "Mexico%20and%20surf");
        uriBuilder.appendQueryParameter("page-size", pageCount);
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("show-elements", "image");
        uriBuilder.appendQueryParameter("show-fields", "headline,thumbnail,trailText");
        uriBuilder.appendQueryParameter("api-key", "d34b30e0-7d4c-42c9-9bc4-0af20234ffc4");

        // convert completed URI to String
        // for example "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&limit=10&minmag=minMagnitude&orderby=orderBy"
        String urlFromUri = uriBuilder.toString();

        // pass concatenated URL to new loader
        ArticleLoader loader = new ArticleLoader(this, urlFromUri);
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

            // this conditional handles the rare edge case of (1) successful network call (2) populate ListView
            // (3) leave app (4) lose internet connection (5) return to app
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
