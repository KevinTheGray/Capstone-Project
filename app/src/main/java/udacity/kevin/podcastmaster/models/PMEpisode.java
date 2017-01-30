package udacity.kevin.podcastmaster.models;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import udacity.kevin.podcastmaster.data.PodcastContract;

public class PMEpisode implements Parcelable {
  private long id;
  private long channelID;
  private String title;
  private String description;
  private String pubDate;
  private String duration;
  private String enclosureURL;
  private String guid;
  private String downloadedMediaFilename;

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
    this.downloadedMediaFilename =
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

  public String getDownloadedMediaFilename() {
    return downloadedMediaFilename;
  }

  public long getChannelID() {
    return channelID;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeLong(this.id);
    parcel.writeLong(this.channelID);
    parcel.writeString(this.title);
    parcel.writeString(this.description);
    parcel.writeString(this.pubDate);
    parcel.writeString(this.duration);
    parcel.writeString(this.enclosureURL);
    parcel.writeString(this.guid);
    parcel.writeString(this.downloadedMediaFilename);
  }

  public static final Parcelable.Creator<PMEpisode> CREATOR = new Parcelable.Creator<PMEpisode>() {
    public PMEpisode createFromParcel(Parcel in) {
      return new PMEpisode(in);
    }
    public PMEpisode[] newArray(int size) {
      return new PMEpisode[size];
    }
  };

  private PMEpisode(Parcel in) {
    this.id = in.readLong();
    this.channelID = in.readLong();
    this.title = in.readString();
    this.description = in.readString();
    this.pubDate = in.readString();
    this.duration = in.readString();
    this.enclosureURL = in.readString();
    this.guid = in.readString();
    this.downloadedMediaFilename = in.readString();
  }
}
