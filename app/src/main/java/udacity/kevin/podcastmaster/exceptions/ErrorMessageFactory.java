package udacity.kevin.podcastmaster.exceptions;

import android.content.Context;
import android.support.annotation.NonNull;

import udacity.kevin.podcastmaster.R;

public class ErrorMessageFactory {
  @NonNull
  public static String GenerateErrorMessage(Context context, int code, String details) {
    if (code == DownloadRSSFeedExceptionCodes.INVALID_URL_SUPPLIED) {
      return context.getString(R.string.download_rss_feed_exception_invalid_url_supplied,
        code, details);
    } else if (code == DownloadRSSFeedExceptionCodes.DATA_RETRIEVAL_FAILED) {
      return context.getString(R.string.download_rss_feed_exception_data_retrieval_failed,
        code, details);
    } else if (code == DownloadRSSFeedExceptionCodes.DATA_PARSING_FAILED) {
      return context.getString(R.string.download_rss_feed_exception_data_parsing_failed,
        code, details);
    } else if (code == DownloadRSSFeedExceptionCodes.DATA_INSERTION_FAILED) {
      return context.getString(R.string.download_rss_feed_exception_data_insertion_failed,
        code);
    }

    return context.getString(R.string.unknown_exception_code, code, details);
  }
}
