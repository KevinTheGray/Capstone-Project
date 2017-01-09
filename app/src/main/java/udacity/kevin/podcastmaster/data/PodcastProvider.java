package udacity.kevin.podcastmaster.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PodcastProvider extends ContentProvider {

  @Override
  public boolean onCreate() {
    return false;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
    return super.query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, String[] strings, String s, String[] strings1, String s1) {
    return null;
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
    return 0;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
    return null;
  }

  @Override
  public int delete(@NonNull Uri uri, String s, String[] strings) {
    return 0;
  }
}
