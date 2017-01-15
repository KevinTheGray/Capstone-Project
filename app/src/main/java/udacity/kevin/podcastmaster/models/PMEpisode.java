package udacity.kevin.podcastmaster.models;

import android.database.Cursor;

import udacity.kevin.podcastmaster.data.PodcastContract;

public class PMEpisode {
  private long id;
  private long channelID;
  private String title;
  private String description;
  private String pubDate;
  private String duration;
  private String enclosureURL;
  private String guid;
  private String downloadedMediaURI;

  public PMEpisode(Cursor cursor) {
    this.id =
      cursor.getLong(cursor.getColumnIndex(PodcastContract.EpisodeEntry._ID));
    this.channelID =
      cursor.getLong(cursor.getColumnIndex(PodcastContract.EpisodeEntry.COLUMN_CHANNEL_ID));
    this.title =
      cursor.getString(cursor.getColumnIndex(PodcastContract.EpisodeEntry.COLUMN_TITLE));
    this.description =
      cursor.getString(cursor.getColumnIndex(PodcastContract.EpisodeEntry.COLUMN_DESCRIPTION));
    this.pubDate =
      cursor.getString(cursor.getColumnIndex(PodcastContract.EpisodeEntry.COLUMN_PUB_DATE));
    this.duration =
      cursor.getString(cursor.getColumnIndex(PodcastContract.EpisodeEntry.COLUMN_DURATION));
    this.enclosureURL =
      cursor.getString(cursor.getColumnIndex(PodcastContract.EpisodeEntry.COLUMN_ENCLOSURE_URL));
    this.guid =
      cursor.getString(cursor.getColumnIndex(PodcastContract.EpisodeEntry.COLUMN_GUID));
    this.downloadedMediaURI =
      cursor.getString(cursor
        .getColumnIndex(PodcastContract.EpisodeEntry.COLUMN_DOWNLOADED_MEDIA_URI));
  }

  public long getID() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getPubDate() {
    return pubDate;
  }

  public String getDescription() {
    return description;
  }

  public String getDuration() {
    return duration;
  }

  public String getEnclosureURL() {
    return enclosureURL;
  }

  public String getGuid() {
    return guid;
  }

  public String getDownloadedMediaURI() {
    return downloadedMediaURI;
  }

  public long getChannelID() {
    return channelID;
  }
}
