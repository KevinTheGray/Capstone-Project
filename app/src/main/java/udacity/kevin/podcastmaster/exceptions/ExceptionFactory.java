package udacity.kevin.podcastmaster.exceptions;

import android.content.Context;
import android.support.annotation.NonNull;

import udacity.kevin.podcastmaster.R;

public class ExceptionFactory {
  @NonNull
  public static Exception GenerateException(Context context, int code) {
    if (code == DownloadRSSFeedExceptionCodes.INVALID_URL_SUPPLIED) {
      return new Exception(
        context.getString(R.string.download_rss_feed_exception_invalid_url_supplied, code));
    }

    return new Exception(
      context.getString(R.string.unknown_exception_code, code));
  }
}
