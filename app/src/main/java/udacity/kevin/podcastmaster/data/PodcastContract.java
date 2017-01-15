package udacity.kevin.podcastmaster.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

// Defines table and column names for the movie database.
public class PodcastContract {
  // The "Content authority" is a name for the entire content provider
  public static final String CONTENT_AUTHORITY = "udacity.kevin.podcastmaster";
  // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
  // the content provider.
  public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
  // Possible paths (appended to base content URI for possible URI's)
  public static final String PATH_CHANNELS = "channels";
  public static final String PATH_EPISODES = "episodes";

  /* Inner class that defines the table contents of the channels table */
  public static final class ChannelEntry implements BaseColumns {
    public static final Uri CONTENT_URI =
      BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHANNELS).build();

    public static final String CONTENT_TYPE =
      ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHANNELS;
    public static final String CONTENT_ITEM_TYPE =
      ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHANNELS;

    // Table name
    public static final String TABLE_NAME = "channels";
    // Title of the channel, stored as a String
    public static final String COLUMN_TITLE = "title";
    // Description of the channel, stored as a String
    public static final String COLUMN_DESCRIPTION = "description";
    // URL of the image for the channel, stored as a String
    public static final String COLUMN_IMAGE_URL = "image_url";
    // URL of the feed for the channel, stored as a String
    public static final String COLUMN_FEED_URL = "feed_url";

    public static Uri buildChannelURI(long id) {
      return ContentUris.withAppendedId(CONTENT_URI, id);
    }
  }

  /* Inner class that defines the table contents of the episodes table */
  public static final class EpisodeEntry implements BaseColumns {
    public static final Uri CONTENT_URI =
      BASE_CONTENT_URI.buildUpon().appendPath(PATH_EPISODES).build();

    public static final String CONTENT_TYPE =
      ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EPISODES;
    public static final String CONTENT_ITEM_TYPE =
      ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EPISODES;

    public static final String TABLE_NAME = "episodes";

    // Title of the episode, stored as a String
    public static final String COLUMN_TITLE = "title";
    // Pubdate of the episode, stored as a String
    public static final String COLUMN_PUB_DATE = "pub_date";
    // Description of the episode, stored as a String
    public static final String COLUMN_DESCRIPTION = "description";
    // Duration of the episode, stored as a String
    public static final String COLUMN_DURATION = "duration";
    // Enclosure URL of the episode, stored as a String
    public static final String COLUMN_ENCLOSURE_URL = "enclosure_url";
    // GUID of the episode, stored as a String
    public static final String COLUMN_GUID = "guid";
    // URI of the downloaded media, stored as a String
    public static final String COLUMN_DOWNLOADED_MEDIA_URI = "download_media_uri";
    // Reference to the relayed Channel, stored as a long
    public static final String COLUMN_CHANNEL_ID = "channel_id";

    public static Uri buildEpisodeURI(long id) {
      return ContentUris.withAppendedId(CONTENT_URI, id);
    }
  }
}
