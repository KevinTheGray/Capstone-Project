package udacity.kevin.podcastmaster.models;

import android.database.Cursor;
import android.database.DatabaseUtils;

import udacity.kevin.podcastmaster.data.PodcastContract;

public class PMChannel {
  private long id;
  private String title;
  private String description;
  private String imageURL;

  public PMChannel(Cursor cursor) {
    DatabaseUtils.dumpCursor(cursor);
    this.id = cursor.getLong(
      cursor.getColumnIndex(PodcastContract.ChannelEntry._ID));
    this.title = cursor.getString(
      cursor.getColumnIndex(PodcastContract.ChannelEntry.COLUMN_TITLE));
    this.description = cursor.getString(
      cursor.getColumnIndex(PodcastContract.ChannelEntry.COLUMN_DESCRIPTION));
    this.imageURL = cursor.getString(
      cursor.getColumnIndex(PodcastContract.ChannelEntry.COLUMN_IMAGE_URL));
  }

  public long getID() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getImageURL() {
    return imageURL;
  }
}
