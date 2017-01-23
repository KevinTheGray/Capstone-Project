package udacity.kevin.podcastmaster.models;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import udacity.kevin.podcastmaster.data.PodcastContract;

public class PMChannel implements Parcelable {
  private long id;
  private String title;
  private String description;
  private String imageURL;

  public PMChannel(Cursor cursor) {
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

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeLong(this.id);
    parcel.writeString(this.title);
    parcel.writeString(this.description);
    parcel.writeString(this.imageURL);
  }

  public static final Parcelable.Creator<PMChannel> CREATOR = new Parcelable.Creator<PMChannel>() {
    public PMChannel createFromParcel(Parcel in) {
      return new PMChannel(in);
    }
    public PMChannel[] newArray(int size) {
      return new PMChannel[size];
    }
  };

  private PMChannel(Parcel in) {
    this.id = in.readLong();
    this.title = in.readString();
    this.description = in.readString();
    this.imageURL = in.readString();
  }
}
