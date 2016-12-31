package udacity.kevin.podcastmaster.networking.downloadrssfeed;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class RSSFeedParser {
  private final String LOG_TAG = "RSSFeedParser";

  public void parse(String xmlFeed) throws UnsupportedEncodingException, XmlPullParserException,
    IOException {
    InputStream inputStream = new ByteArrayInputStream(xmlFeed.getBytes("UTF-8"));
    boolean insideChannel = false;
    boolean insideItem = false;
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
                Log.d(LOG_TAG, "item duration");
              }
            } else {
              if (tagName.equals("title")) {
                Log.d(LOG_TAG, "item title");
              } else if (tagName.equals("pubDate")) {
                Log.d(LOG_TAG, "item pubDate");
              } else if (tagName.equals("description")) {
                Log.d(LOG_TAG, "item description");
              } else if (tagName.equals("enclosure")) {
                Log.d(LOG_TAG, "item enclosure");
              }
            }
          } else if (insideChannel) {
            if (parser.getPrefix() != null && parser.getPrefix().equals("itunes")) {
              if (tagName.equals("summary")) {
                Log.d(LOG_TAG, "channel summary");
              } else if (tagName.equals("image")) {
                Log.d(LOG_TAG, "channel image");
              }
            } else {
              if (tagName.equals("title")) {
                Log.d(LOG_TAG, "channel title");
              }
            }
          }
        } else if (currentEvent == XmlPullParser.END_TAG) {
          String tagName = parser.getName();
          if (tagName.equals("channel")) {
            insideChannel = false;
            Log.d(LOG_TAG, "End channel");
          } else if (tagName.equals("item")) {
            insideItem = false;
            Log.d(LOG_TAG, "End item");
          }
        }
        currentEvent = parser.next();
      }
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
