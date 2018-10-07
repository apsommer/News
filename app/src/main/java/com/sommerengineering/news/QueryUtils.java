package com.sommerengineering.news;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

// helper methods related to requesting and receiving news article data from The Guardian API
public final class QueryUtils {

    // simple tag for log messages
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    // constructor is deliberately empty as no objects of this class will ever exist
    private QueryUtils() {}

    // query the The Guardian database and return an ArrayList of Article objects
    // this is the only public method in this class, used by AsyncTaskLoader in ArticleLoader
    public static ArrayList<Article> fetchArticleData(String requestUrl) {

        // transform url string to URL object
        URL url = createUrl(requestUrl);

        // initialize raw JSON string to null
        String jsonResponse = null;

        // perform HTTP request to the URL and receive a JSON response back
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream.", e);
        }

        // extract relevant fields from the JSON response and create a list of Articles
        ArrayList<Article> articles = extractArticlesFromJSON(jsonResponse);

        // Return the {@link Event}
        return articles;
    }

    // returns URL object from a given string URL
    private static URL createUrl(String stringUrl) {

        // initialize returned object to null
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error creating URL.", e);
        }
        return url;
    }

    // Make an HTTP request to the given URL and return the response string
    private static String makeHttpRequest(URL url) throws IOException {

        // initialize raw JSON string to empty
        String jsonResponse = "";

        // if the URL is null then return early
        if (url == null) {
            return jsonResponse;
        }

        // initialize objects for connection and stream
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {

            // open connection, set timeouts, set request method, connect
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000); // milliseconds
            urlConnection.setConnectTimeout(15000); // milliseconds
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // check response code of HTTP request
            // 200 means success
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {

                // log HTTP response code
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());

            }

        // IOException is thrown by getInputStream() if something goes wrong parsing the characters
        } catch (IOException e) {

            // log exception stack trace
            Log.e(LOG_TAG, "Problem retrieving the article JSON results.", e);

        // disconnect from url and close stream
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }

        // return data type is String
        return jsonResponse;
    }

    // Convert the InputStream into a String which contains the entire raw JSON response
    private static String readFromStream(InputStream inputStream) throws IOException {

        // StringBuilder is mutable, a convenient way to construct Strings
        StringBuilder output = new StringBuilder();

        // check that inputStream exists
        if (inputStream != null) {

            // define character set as UTF-8
            // this is at the byte level where each byte defines a single character
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));

            // parsing a line of characters is much faster than one at a time
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();

            // append lines to StringBuilder while there are still lines left to read
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }

        // convert mutable StringBuilder to immutable String
        return output.toString();
    }


    // Return a list of Earthquake objects built up from parsing a JSON response.
    private static ArrayList<Article> extractArticlesFromJSON(String jsonResponse) {

        // initialize an empty ArrayList
        ArrayList<Article> articles = new ArrayList<>();

        // Try to parse the raw jsonResponse String
        try {

            // go down two levels of JSON payload
            JSONObject root = new JSONObject(jsonResponse);
            JSONObject response = root.getJSONObject("response");
            JSONArray results = response.getJSONArray("results");

            // loop through all results (results = news articles)
            for (int i = 0; i < results.length(); i++) {

                // current news article
                JSONObject currentResult = results.getJSONObject(i);

                // base keys
                JSONObject fields = currentResult.getJSONObject("fields");

                // get desired attributes from JSON key : value pairs
                String section = currentResult.getString("sectionName");
                String date = currentResult.getString("webPublicationDate");
                String url = currentResult.getString("webUrl");
                String title = fields.getString("headline");
                String body = fields.getString("trailText");

                // not all articles contain an author name
                String firstName;
                String lastName;
                try {
                    JSONObject tags = currentResult.getJSONArray("tags").getJSONObject(0);
                    firstName = tags.getString("firstName");
                    lastName = tags.getString("lastName");
                }
                catch (JSONException e) {
                    firstName = "";
                    lastName = "";
                }

                // add data to new Article object and store in ArrayList
                articles.add(new Article(title, body, firstName, lastName, date, section, url));

            }

        } catch (JSONException e) {

            // log exception stack trace
            Log.e(LOG_TAG, "Problem parsing the article JSON results.", e);
        }

        // return the list of news articles
        return articles;
    }

}
