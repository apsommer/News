package com.sommerengineering.news;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

// loads a list of news article metadata using a background AsyncTask to perform a network URL request
public class ArticleLoader extends AsyncTaskLoader<List<Article>> {

    // simple tag for log messages
    private static final String LOG_TAG = ArticleLoader.class.getSimpleName();

    // initialize state variable for url String
    private String mUrl;

    public ArticleLoader(Context context, String url) {

        // inherit loader initialization configuration from superclass AsyncTaskLoader
        super(context);

        // this loader has only one designated url address
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {

        // this method is required to trigger loadInBackground()
        forceLoad();

    }

    @Override
    public List<Article> loadInBackground() {

        // check that the input parameter has at least one string
        if (mUrl == null) {
            return null;
        }

        // perform the HTTP request for article data and process the JSON response
        List<Article> earthquakes = QueryUtils.fetchArticleData(mUrl);

        return earthquakes;

    }

}
