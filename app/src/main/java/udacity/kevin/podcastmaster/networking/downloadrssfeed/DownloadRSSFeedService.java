package udacity.kevin.podcastmaster.networking.downloadrssfeed;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.data.PodcastCRUDHelper;
import udacity.kevin.podcastmaster.exceptions.DownloadRSSFeedExceptionCodes;
import udacity.kevin.podcastmaster.models.RSSChannel;

public class DownloadRSSFeedService extends IntentService {

  public final String LOG_TAG = "DownloadRSSFeedService";

  public final static String BROADCAST_UPDATE_ACTION =
    "udacity.kevin.podcastmaster.downloadrssfeedservice.UPDATE";
  public final static String BROADCAST_FINISHED_ACTION =
    "udacity.kevin.podcastmaster.downloadrssfeedservice.FINISHED";

  public final static String INTENT_EXTRA_KEY_RSS_URL =
    "udacity.kevin.podcastmaster.downloadrssfeedservice.RSS_URL_KEY";
  public final static String INTENT_EXTRA_KEY_ERROR_CODE =
    "udacity.kevin.podcastmaster.downloadrssfeedservice.ERROR_CODE_KEY";
  public final static String INTENT_EXTRA_KEY_DETAILED_ERROR_MESSAGE =
    "udacity.kevin.podcastmaster.downloadrssfeedservice.DETAILED_ERROR_MESSAGE_KEY";
  public final static String INTENT_EXTRA_KEY_FINISHED_SUCCESS =
    "udacity.kevin.podcastmaster.downloadrssfeedservice.FINISHED_SUCCESS_KEY";
  public final static String INTENT_EXTRA_KEY_UPDATE_MESSAGE =
    "udacity.kevin.podcastmaster.downloadrssfeedservice.UPDATE_MESSAGE";
  public final static String INTENT_EXTRA_KEY_SUCCESS_MESSAGE =
    "udacity.kevin.podcastmaster.downloadrssfeedservice.SUCCESS_MESSAGE";

  public DownloadRSSFeedService() {
    super("DownloadRSSFeedService");
  }
  @Override
  protected void onHandleIntent(Intent intent) {

    Intent finishedIntent = new Intent(BROADCAST_FINISHED_ACTION);
    Intent updateIntent = new Intent(BROADCAST_UPDATE_ACTION);

    String rssURLString = intent.getStringExtra(INTENT_EXTRA_KEY_RSS_URL).toLowerCase();
    URL rssURL;
    try {
      rssURL = new URL(rssURLString);
    } catch (MalformedURLException malformedURLException) {
      finishedIntent.putExtra(INTENT_EXTRA_KEY_ERROR_CODE,
        DownloadRSSFeedExceptionCodes.INVALID_URL_SUPPLIED);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_FINISHED_SUCCESS, false);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_DETAILED_ERROR_MESSAGE,
        malformedURLException.getMessage());
      LocalBroadcastManager.getInstance(this).sendBroadcast(finishedIntent);
      return;
    }

    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    StringBuilder builder = new StringBuilder();
    try {
      // Create the request to OpenWeatherMap, and open the connection
      urlConnection = (HttpURLConnection) rssURL.openConnection();
      urlConnection.setRequestMethod("GET");
      urlConnection.connect();

      if (urlConnection.getResponseCode() != 200) {
        String errorMessage = getString(R.string.bad_http_response_code,
          urlConnection.getResponseCode());
        throw new IOException(errorMessage);
      }

      // Read the input stream into a String
      InputStream inputStream = urlConnection.getInputStream();
      builder = new StringBuilder();
      reader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }

    } catch (IOException ioException) {
      finishedIntent.putExtra(INTENT_EXTRA_KEY_ERROR_CODE,
        DownloadRSSFeedExceptionCodes.DATA_RETRIEVAL_FAILED);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_FINISHED_SUCCESS, false);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_DETAILED_ERROR_MESSAGE,
        ioException.getMessage());
      LocalBroadcastManager.getInstance(this).sendBroadcast(finishedIntent);
      return;
    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ioException) {
          // I believe this can be ignored, probably just a leak will occur
          Log.e(LOG_TAG, ioException.getMessage());
        }
      }
    }

    Log.d(LOG_TAG, builder.toString());
    RSSFeedParser rssFeedParser = new RSSFeedParser();
    RSSChannel rssChannel;
    try {
      updateIntent.putExtra(INTENT_EXTRA_KEY_UPDATE_MESSAGE,
        getResources().getString(R.string.add_feed_progress_dialog_xml_processing));
      LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
      rssChannel = rssFeedParser.parse(builder.toString(), rssURLString, this);
    } catch (Exception e) {
      finishedIntent.putExtra(INTENT_EXTRA_KEY_ERROR_CODE,
        DownloadRSSFeedExceptionCodes.DATA_PARSING_FAILED);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_FINISHED_SUCCESS, false);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_DETAILED_ERROR_MESSAGE,
        e.getMessage());
      LocalBroadcastManager.getInstance(this).sendBroadcast(finishedIntent);
      return;
    }

    PodcastCRUDHelper podcastCRUDHelper = new PodcastCRUDHelper(getContentResolver());
    updateIntent.putExtra(INTENT_EXTRA_KEY_UPDATE_MESSAGE,
      getResources().getString(R.string.add_feed_progress_dialog_saving_content));
    LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);

    HashMap<String, Object> iouReturnValues =
      podcastCRUDHelper.insertOrUpdateRSSChannel(rssChannel);
    Uri channelUri = (Uri) iouReturnValues.get(PodcastCRUDHelper.URI_RETURN_KEY);
    boolean wasInserted = (boolean) iouReturnValues.get(PodcastCRUDHelper.URI_WAS_INSERTED_KEY);
    if (channelUri == null) {
      finishedIntent.putExtra(INTENT_EXTRA_KEY_ERROR_CODE,
        DownloadRSSFeedExceptionCodes.DATA_INSERTION_FAILED);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_FINISHED_SUCCESS, false);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_DETAILED_ERROR_MESSAGE, "");
      LocalBroadcastManager.getInstance(this).sendBroadcast(finishedIntent);
      return;
    }

    finishedIntent.putExtra(INTENT_EXTRA_KEY_FINISHED_SUCCESS, true);
    if (wasInserted) {
      finishedIntent.putExtra(INTENT_EXTRA_KEY_SUCCESS_MESSAGE,
        getString(R.string.add_feed_success_inserted_dialog_content, rssChannel.getTitle()));
    } else {
      finishedIntent.putExtra(INTENT_EXTRA_KEY_SUCCESS_MESSAGE,
        getString(R.string.add_feed_success_updated_dialog_content, rssChannel.getTitle()));
    }
    LocalBroadcastManager.getInstance(this).sendBroadcast(finishedIntent);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(LOG_TAG, "Destroyed");
  }
}
