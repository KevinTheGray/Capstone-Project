package udacity.kevin.podcastmaster.networking.downloadrssfeed;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import udacity.kevin.podcastmaster.exceptions.InvalidModelException;
import udacity.kevin.podcastmaster.models.RSSChannel;
import udacity.kevin.podcastmaster.models.RSSEpisode;

public class RSSFeedParser {
  private final String LOG_TAG = "RSSFeedParser";

  public RSSChannel parse(String xmlFeed, Context context) throws
    UnsupportedEncodingException, XmlPullParserException, IOException, InvalidModelException {
    InputStream inputStream = new ByteArrayInputStream(xmlFeed.getBytes("UTF-8"));
    RSSChannel returnedRSSChannel = null;
    ArrayList<RSSEpisode> returnedRSSEpisodes = new ArrayList<>();
    boolean insideChannel = false;
    boolean insideItem = false;
    String channelTitle = null;
    String channelSummary = null;
    String channelImageURL = null;
    String channelDescription = null;
    String currentItemTitle = null;
    String currentItemPubDate = null;
    String currentItemDescription = null;
    String currentItemEnclosureURL = null;
    String currentItemDuration = null;
    String currentItemGUID = null;
    try {
      XmlPullParser parser = Xml.newPullParser();
      parser.setInput(inputStream, null);
      int currentEvent = parser.next();
      while (currentEvent != XmlPullParser.END_DOCUMENT) {
        if (currentEvent == XmlPullParser.START_TAG) {
          String tagName = parser.getName();
          if (tagName.equals("channel")) {
            insideChannel = true;
            Log.d(LOG_TAG, "Start channel");
          } else if (tagName.equals("item")) {
            insideItem = true;
            Log.d(LOG_TAG, "Start item");
          }
          if (insideItem) {
            if (parser.getPrefix() != null && parser.getPrefix().equals("itunes")) {
              if (tagName.equals("duration")) {
                currentItemDuration = parser.nextText();
              }
            } else {
              if (tagName.equals("title")) {
                currentItemTitle = parser.nextText();
              } else if (tagName.equals("pubDate")) {
                currentItemPubDate = parser.nextText();
              } else if (tagName.equals("description")) {
                currentItemDescription = parser.nextText();
              } else if (tagName.equals("enclosure")) {
                currentItemEnclosureURL = parser.getAttributeValue(null, "url");
              } else if (tagName.equals("guid")) {
                currentItemGUID = parser.nextText();
              }
            }
          } else if (insideChannel) {
            if (parser.getPrefix() != null && parser.getPrefix().equals("itunes")) {
              if (tagName.equals("summary")) {
                channelSummary = parser.nextText();
              } else if (tagName.equals("image")) {
                channelImageURL = parser.getAttributeValue(null, "href");
              }
            } else {
              if (tagName.equals("title")) {
                channelTitle = parser.nextText();
              } else if (tagName.equals("description")) {
                channelDescription = parser.nextText();
              }
            }
          }

        } else if (currentEvent == XmlPullParser.END_TAG) {
          String tagName = parser.getName();
          if (tagName.equals("channel")) {
            insideChannel = false;
            Log.d(LOG_TAG, "End channel");
          } else if (tagName.equals("item")) {
            Log.d(LOG_TAG, "item title: " + currentItemTitle);
            Log.d(LOG_TAG, "item pubDate: " + currentItemPubDate);
            Log.d(LOG_TAG, "item description: " + currentItemDescription);
            Log.d(LOG_TAG, "item duration: " + currentItemDuration);
            Log.d(LOG_TAG, "item enclosure: " + currentItemEnclosureURL);
            Log.d(LOG_TAG, "item guid: " + currentItemGUID);
            try {
              RSSEpisode rssEpisode = new RSSEpisode(currentItemTitle, currentItemPubDate,
                currentItemDescription, currentItemDuration, currentItemEnclosureURL, currentItemGUID, context);
              returnedRSSEpisodes.add(rssEpisode);
            } catch (InvalidModelException invalidModelException) {
              Log.e(LOG_TAG, invalidModelException.getMessage());
            }
            insideItem = false;
            currentItemDescription = null;
            currentItemDuration = null;
            currentItemEnclosureURL = null;
            currentItemPubDate = null;
            currentItemTitle = null;
            currentItemGUID = null;
            Log.d(LOG_TAG, "End item");
          }
        }
        currentEvent = parser.next();
      }
      Log.d(LOG_TAG, "channel title: " + channelTitle);
      Log.d(LOG_TAG, "channel summary: " + channelSummary);
      Log.d(LOG_TAG, "channel description: " + channelDescription);
      Log.d(LOG_TAG, "channel image: " + channelImageURL);
      returnedRSSChannel = new RSSChannel(channelTitle, channelDescription, channelSummary,
        channelImageURL, returnedRSSEpisodes, context);
      return returnedRSSChannel;
    } finally {
      try {
        inputStream.close();
      } catch (IOException ioException) {
        // Should be safe to ignore
        Log.e(LOG_TAG, ioException.getMessage());
      }
    }

  }
}
