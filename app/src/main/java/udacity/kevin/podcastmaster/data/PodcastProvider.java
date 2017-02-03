package udacity.kevin.podcastmaster.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PodcastProvider extends ContentProvider {

  // The URI Matcher used by this content provider.
  private static final UriMatcher sUriMatcher = buildUriMatcher();
  private PodcastDBHelper mOpenHelper;

  static final int CHANNELS = 100;
  static final int EPISODES = 200;

  @Override
  public boolean onCreate() {
    mOpenHelper = new PodcastDBHelper(getContext());
    return true;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sortOrder,
                      CancellationSignal cancellationSignal) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    Cursor retCursor;

    if (match == CHANNELS) {
      retCursor = mOpenHelper.getReadableDatabase().query(
        PodcastContract.ChannelEntry.TABLE_NAME, projection, selection, selectionArgs, null,
        null, sortOrder);
      if (retCursor != null && getContext() != null) {
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
      }
    } else if (match == EPISODES) {
      retCursor = mOpenHelper.getReadableDatabase().query(
        PodcastContract.EpisodeEntry.TABLE_NAME, projection, selection, selectionArgs, null,
        null, sortOrder);
      if (retCursor != null && getContext() != null) {
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
      }
    } else {
      throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
    return retCursor;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, String[] strings, String s, String[] strings1, String s1) {
    return null;
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    int updateCount = 0;
    if (match == CHANNELS) {
      updateCount = db.update(PodcastContract.ChannelEntry.TABLE_NAME, contentValues, s, strings);
    } else if (match == EPISODES) {
      updateCount = db.update(PodcastContract.EpisodeEntry.TABLE_NAME, contentValues, s, strings);
    } else {
      throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
    if (updateCount != 0) {
      Context context = getContext();
      if (context != null) {
       context.getContentResolver().notifyChange(uri, null);
      }
    }
    return updateCount;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    Uri returnUri = null;

    if (match == CHANNELS) {
      long _id = db.insert(PodcastContract.ChannelEntry.TABLE_NAME, null, contentValues);
      if ( _id > 0 )
        returnUri = PodcastContract.ChannelEntry.buildChannelURI(_id);
      else
        throw new android.database.SQLException("Failed to insert row into " + uri);
    } else if (match == EPISODES) {
      long _id = db.insert(PodcastContract.EpisodeEntry.TABLE_NAME, null, contentValues);
      if ( _id > 0 )
        returnUri = PodcastContract.EpisodeEntry.buildEpisodeURI(_id);
      else
        throw new android.database.SQLException("Failed to insert row into " + uri);
    } else {
      throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    return returnUri;
  }

  @Override
  public int delete(@NonNull Uri uri, String s, String[] strings) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    int deletedCount = 0;
    if (match == CHANNELS) {
      deletedCount = db.delete(PodcastContract.ChannelEntry.TABLE_NAME, s, strings);
    } else if (match == EPISODES) {
      deletedCount = db.delete(PodcastContract.EpisodeEntry.TABLE_NAME, s, strings);
    } else {
      throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
    if (deletedCount != 0) {
      Context context = getContext();
      if (context != null) {
        context.getContentResolver().notifyChange(PodcastContract.ChannelEntry.CONTENT_URI, null);
				context.getContentResolver().notifyChange(PodcastContract.EpisodeEntry.CONTENT_URI, null);
      }
    }
    return deletedCount;
  }

  static UriMatcher buildUriMatcher() {
    final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    final String authority = PodcastContract.CONTENT_AUTHORITY;

    // For each type of URI you want to add, create a corresponding code.
    matcher.addURI(authority, PodcastContract.PATH_CHANNELS, CHANNELS);
    matcher.addURI(authority, PodcastContract.PATH_EPISODES, EPISODES);

    return matcher;
  }

}
