package udacity.kevin.podcastmaster.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PodcastDBHelper extends SQLiteOpenHelper {
  private static final int DATABASE_VERSION = 1;
  static final String DATABASE_NAME = "podcasts.db";

  public PodcastDBHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onOpen(SQLiteDatabase db) {
    super.onOpen(db);
    if (!db.isReadOnly()) {
      // Enable foreign key constraints
      db.execSQL("PRAGMA foreign_keys=ON;");
    }
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    final String SQL_CREATE_CHANNEL_TABLE =
      "CREATE TABLE " + PodcastContract.ChannelEntry.TABLE_NAME + " (" +
        PodcastContract.ChannelEntry._ID + " INTEGER PRIMARY KEY," +
        PodcastContract.ChannelEntry.COLUMN_FEED_URL + " TEXT UNIQUE NOT NULL, " +
        PodcastContract.ChannelEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
        PodcastContract.ChannelEntry.COLUMN_DESCRIPTION + " TEXT, " +
        PodcastContract.ChannelEntry.COLUMN_IMAGE_URL + " TEXT " +
        " );";

    final String SQL_CREATE_EPISODE_TABLE = "CREATE TABLE "
      + PodcastContract.EpisodeEntry.TABLE_NAME + " (" +
      PodcastContract.EpisodeEntry._ID + " INTEGER PRIMARY KEY," +
      PodcastContract.EpisodeEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
      PodcastContract.EpisodeEntry.COLUMN_PUB_DATE + " TEXT NOT NULL, " +
      PodcastContract.EpisodeEntry.COLUMN_ENCLOSURE_URL + " TEXT NOT NULL, " +
      PodcastContract.EpisodeEntry.COLUMN_DOWNLOADED_MEDIA_URI + " TEXT, " +
      PodcastContract.EpisodeEntry.COLUMN_DESCRIPTION + " TEXT, " +
      PodcastContract.EpisodeEntry.COLUMN_DURATION + " TEXT, " +
      PodcastContract.EpisodeEntry.COLUMN_GUID + " TEXT UNIQUE NOT NULL, " +
      PodcastContract.EpisodeEntry.COLUMN_CHANNEL_ID + " INTEGER NOT NULL, " +
      " FOREIGN KEY (" + PodcastContract.EpisodeEntry.COLUMN_CHANNEL_ID + ") REFERENCES " +
      PodcastContract.ChannelEntry.TABLE_NAME + " (" + PodcastContract.ChannelEntry._ID + "));";

    sqLiteDatabase.execSQL(SQL_CREATE_CHANNEL_TABLE);
    sqLiteDatabase.execSQL(SQL_CREATE_EPISODE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    // For now, if the upgrade happens, i just drop all the data and that's it.  shouldn't happen.
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PodcastContract.ChannelEntry.TABLE_NAME);
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PodcastContract.EpisodeEntry.TABLE_NAME);
    onCreate(sqLiteDatabase);

  }
}
