package com.sommerengineering.news;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ArticleAdapter extends ArrayAdapter<Article> {

    // simple tag for log messages
    private static final String LOG_TAG = ArrayAdapter.class.getSimpleName();

    // constructor
    public ArticleAdapter(Context context, ArrayList<Article> articles) {

        // call superclass ArrayAdapter constructor
        // second argument for populating a single TextView (the default for ArrayAdapter)
        // since a custom layout is inflated in getView() this second argument is arbitrary, for initialization only
        super(context, 0, articles);

    }

    // must override to inflate anything other than the default single TextView expected by ArrayAdapter
    // parent is the ListView in earthquake_activity
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // rename input argument for clarity
        View listItemView = convertView;

        // if the passed view does not exist (therefore it is not being recycled) then inflate it from list_item
        if(listItemView  == null) {
            listItemView  = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        // get the Article object at this position in the ArrayList
        Article currentArticle = getItem(position);

        // get View references from list_item
        TextView titleTextView = (TextView) listItemView.findViewById(R.id.title);
        TextView trailTextView = (TextView) listItemView.findViewById(R.id.body);
        TextView firstNameTextView = (TextView) listItemView.findViewById(R.id.first_name);
        TextView lastNameTextView = (TextView) listItemView.findViewById(R.id.last_name);
        TextView dateTextView = (TextView) listItemView.findViewById(R.id.date);
        TextView sectionTextView = (TextView) listItemView.findViewById(R.id.section);

        // get attributes from the current Article and set to Views
        titleTextView.setText(currentArticle.getTitle());
        sectionTextView.setText(currentArticle.getSection());

        // the author name can be lowercase, or not exist at all in the JSON metadata
        try {

            // ensure that first name starts with a capital letter
            String firstName = currentArticle.getFirstName();
            String capitalizedFirstName = firstName.substring(0,1).toUpperCase() + firstName.substring(1);
            firstNameTextView.setText(capitalizedFirstName);

            // ensure that last name starts with a capital letter
            String lastName = currentArticle.getLastName();
            String capitalizedLastName = lastName.substring(0,1).toUpperCase() + lastName.substring(1);
            lastNameTextView.setText(capitalizedLastName);

        }
        catch (StringIndexOutOfBoundsException e) {

            // hide the TextViews as there is no author name
            firstNameTextView.setVisibility(View.GONE);
            lastNameTextView.setVisibility(View.GONE);

        }

        // trailText string contains HTML tags
        trailTextView.setText(Html.fromHtml(currentArticle.getTrailText()));

        // convert timestamp to simple date with helper method
        dateTextView.setText(formatDate(currentArticle.getDate()));

        // return the inflated view to fragment
        return listItemView;

    }

    // coverts datetime timestamp to simple date only format
    private String formatDate(String timestamp) {

        // expected datetime format
        String expectedPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        SimpleDateFormat expectedFormatter = new SimpleDateFormat(expectedPattern);

        // change to simpler date only
        String simplePattern = "LLL d, yyyy";
        SimpleDateFormat simpleFormatter = new SimpleDateFormat(simplePattern);

        // unexpected timestamp format will cause parsing error
        try {
            Date date = expectedFormatter.parse(timestamp);
            String simpleDate= simpleFormatter.format(date);
            return simpleDate;
        }
        catch (ParseException e) {
            Log.e(LOG_TAG, "Error formatting timestamp.", e);
            return timestamp;
        }

    }

}
