package udacity.kevin.podcastmaster.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

import udacity.kevin.podcastmaster.models.PMChannel;
import udacity.kevin.podcastmaster.models.PMEpisode;
import udacity.kevin.podcastmaster.models.RSSChannel;
import udacity.kevin.podcastmaster.models.RSSEpisode;

public class PodcastCRUDHelper {
  final private ContentResolver contentResolver;
  final private String LOG_TAG = "PodcastCRUDHelper";
  final public static String URI_RETURN_KEY = "URI";
  final public static String URI_WAS_INSERTED_KEY = "WAS_UPDATED";

  public PodcastCRUDHelper(ContentResolver contentResolver) {
    this.contentResolver = contentResolver;
  }

  private PMChannel findChannelByURLOrNull(String url) {
    Cursor cursor = contentResolver.query(PodcastContract.ChannelEntry.CONTENT_URI,
      new String[]{PodcastContract.ChannelEntry._ID, PodcastContract.ChannelEntry.COLUMN_TITLE,
        PodcastContract.ChannelEntry.COLUMN_DESCRIPTION,
        PodcastContract.ChannelEntry.COLUMN_FEED_URL,
        PodcastContract.ChannelEntry.COLUMN_IMAGE_URL},
      PodcastContract.ChannelEntry.COLUMN_FEED_URL + " = ?",
      new String[]{url},
      null);

    if (cursor == null || !cursor.moveToFirst()) {
      return null;
    } else {
      PMChannel pmChannel = new PMChannel(cursor);
      cursor.close();
      return pmChannel;
    }
  }

  private PMEpisode findEpisodeByGUIDOrNull(String guid) {
    Cursor cursor = contentResolver.query(PodcastContract.EpisodeEntry.CONTENT_URI,
      new String[]{PodcastContract.EpisodeEntry._ID,
        PodcastContract.EpisodeEntry.COLUMN_TITLE,
        PodcastContract.EpisodeEntry.COLUMN_DESCRIPTION,
        PodcastContract.EpisodeEntry.COLUMN_PUB_DATE,
        PodcastContract.EpisodeEntry.COLUMN_ENCLOSURE_URL,
        PodcastContract.EpisodeEntry.COLUMN_DURATION,
        PodcastContract.EpisodeEntry.COLUMN_DOWNLOADED_MEDIA_URI,
        PodcastContract.EpisodeEntry.COLUMN_CHANNEL_ID,
        PodcastContract.EpisodeEntry.COLUMN_GUID},
      PodcastContract.EpisodeEntry.COLUMN_GUID + " = ?",
      new String[]{guid},
      null);

    if (cursor == null || !cursor.moveToFirst()) {
      return null;
    } else {
      PMEpisode pmEpisode = new PMEpisode(cursor);
      cursor.close();
      return pmEpisode;
    }
  }

  public HashMap<String, Object> insertOrUpdateRSSChannel(RSSChannel rssChannel) {
    ContentValues channelValues = new ContentValues();
    channelValues.put(PodcastContract.ChannelEntry.COLUMN_TITLE, rssChannel.getTitle());
    channelValues.put(PodcastContract.ChannelEntry.COLUMN_DESCRIPTION, rssChannel.getDescription());
    channelValues.put(PodcastContract.ChannelEntry.COLUMN_FEED_URL, rssChannel.getRSSURL());
    channelValues.put(PodcastContract.ChannelEntry.COLUMN_IMAGE_URL, rssChannel.getImageURL());

    PMChannel pmChannel = findChannelByURLOrNull(rssChannel.getRSSURL());
    HashMap<String, Object> returnedHashMap = new HashMap<>();
    Uri returnedChannelUri = null;
    boolean wasInserted = false;
    if (pmChannel == null) {
      wasInserted = true;
      returnedChannelUri =
        contentResolver.insert(PodcastContract.ChannelEntry.CONTENT_URI, channelValues);
    } else {
      Uri channelURI = PodcastContract.ChannelEntry.buildChannelURI(pmChannel.getID());
      int updateCount = contentResolver.update(PodcastContract.ChannelEntry.CONTENT_URI,
        channelValues, PodcastContract.ChannelEntry._ID + " = ?",
        new String[]{String.valueOf(pmChannel.getID())});
      if (updateCount == 1) {
        returnedChannelUri = channelURI;
      }
    }
    if (returnedChannelUri != null && pmChannel != null) {
      for (RSSEpisode rssEpisode : rssChannel.getEpisodes()) {
        ContentValues episodeValues = new ContentValues();
        episodeValues.put(PodcastContract.EpisodeEntry.COLUMN_TITLE, rssEpisode.getTitle());
        episodeValues.put(PodcastContract.EpisodeEntry.COLUMN_DESCRIPTION,
          rssEpisode.getDescription());
        episodeValues.put(PodcastContract.EpisodeEntry.COLUMN_DURATION, rssEpisode.getDuration());
        episodeValues.put(PodcastContract.EpisodeEntry.COLUMN_PUB_DATE, rssEpisode.getPubDate());
        episodeValues.put(PodcastContract.EpisodeEntry.COLUMN_ENCLOSURE_URL,
          rssEpisode.getEnclosureURL());
        episodeValues.put(PodcastContract.EpisodeEntry.COLUMN_GUID, rssEpisode.getGuid());
        episodeValues.put(PodcastContract.EpisodeEntry.COLUMN_CHANNEL_ID, pmChannel.getID());

        PMEpisode pmEpisode = findEpisodeByGUIDOrNull(rssEpisode.getGuid());
        if (pmEpisode == null) {
          Uri episodeURI =
            contentResolver.insert(PodcastContract.EpisodeEntry.CONTENT_URI, episodeValues);
          if (episodeURI == null) {
            Log.e(LOG_TAG, "Failed to insert an episode from " + rssChannel.getTitle()
              + "with GUID " + rssEpisode.getGuid());
          }
        } else {
          episodeValues.put(PodcastContract.EpisodeEntry.COLUMN_DOWNLOADED_MEDIA_URI,
            pmEpisode.getDownloadedMediaURI());
          int updateCount = contentResolver.update(PodcastContract.EpisodeEntry.CONTENT_URI,
            episodeValues, PodcastContract.EpisodeEntry._ID + " = ?",
            new String[]{String.valueOf(pmEpisode.getID())});
          if (updateCount != 1) {
            Log.e(LOG_TAG, "Failed to insert an episode from " + rssChannel.getTitle()
              + "with GUID " + rssEpisode.getGuid());
          }
        }
      }
    }
    returnedHashMap.put(URI_RETURN_KEY, returnedChannelUri);
    returnedHashMap.put(URI_WAS_INSERTED_KEY, wasInserted);
    return returnedHashMap;
  }

  public void deletePMChannel(PMChannel pmChannel) {
    contentResolver.delete(PodcastContract.ChannelEntry.CONTENT_URI,
      PodcastContract.ChannelEntry._ID + " = ?",
      new String[] {String.valueOf(pmChannel.getID())});
  }
}
