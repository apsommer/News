package com.sommerengineering.news;

// custom Article object holds metadata for an given news article
public class Article {

    // attributes
    private String mTitle; // title
    private String mTrailText; // body
    private String mFirstName; // author first name
    private String mLastName; // author last name
    private String mDate; // publication date
    private String mSection; // section classification name
    private String mUrl; // url for specific USGS event page

    // constructor
    public Article(String title, String trailText, String firstName, String lastName, String date, String section, String url) {
        mTitle = title;
        mTrailText = trailText;
        mFirstName = firstName;
        mLastName = lastName;
        mDate = date;
        mSection = section;
        mUrl = url;
    }

    // getters
    public String getTitle() {
        return mTitle;
    }
    public String getTrailText() {
        return mTrailText;
    }
    public String getFirstName() {
        return mFirstName;
    }
    public String getLastName() {
        return mLastName;
    }
    public String getDate() {
        return mDate;
    }
    public String getSection() {
        return mSection;
    }
    public String getUrl() {
        return mUrl;
    }

}
