package udacity.kevin.podcastmaster.networking.downloadcontent;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.exceptions.DownloadEpisodeExceptionCodes;
import udacity.kevin.podcastmaster.models.PMEpisode;

public class DownloadEpisodeService extends IntentService {

  public final static String BROADCAST_UPDATE_ACTION =
    "udacity.kevin.podcastmaster.downloadepisodeservice.UPDATE";
  public final static String BROADCAST_FINISHED_ACTION =
    "udacity.kevin.podcastmaster.downloadepisodeservice.FINISHED";

  public final static String INTENT_EXTRA_KEY_PM_EPISODE =
    "udacity.kevin.podcastmaster.downloadepisodeservice.PM_EPISODE_KEY";
  public final static String INTENT_EXTRA_KEY_ERROR_CODE =
    "udacity.kevin.podcastmaster.downloadepisodeservice.ERROR_CODE_KEY";
  public final static String INTENT_EXTRA_KEY_DETAILED_ERROR_MESSAGE =
    "udacity.kevin.podcastmaster.downloadepisodeservice.DETAILED_ERROR_MESSAGE_KEY";
  public final static String INTENT_EXTRA_KEY_FINISHED_SUCCESS =
    "udacity.kevin.podcastmaster.downloadepisodeservice.FINISHED_SUCCESS_KEY";
  public final static String INTENT_EXTRA_KEY_UPDATE_MESSAGE =
    "udacity.kevin.podcastmaster.downloadepisodeservice.UPDATE_MESSAGE";
  public final static String INTENT_EXTRA_KEY_SUCCESS_MESSAGE =
    "udacity.kevin.podcastmaster.downloadepisodeservice.SUCCESS_MESSAGE";

  public final String LOG_TAG = "DownloadEpisodeService";

  public DownloadEpisodeService() {
    super("DownloadEpisodeService");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(LOG_TAG, "onStartCommand");
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Intent finishedIntent = new Intent(BROADCAST_FINISHED_ACTION);
    Intent updateIntent = new Intent(BROADCAST_UPDATE_ACTION);
    PMEpisode pmEpisode = intent.getParcelableExtra(INTENT_EXTRA_KEY_PM_EPISODE);

    URL episodeURL = null;
    HttpURLConnection httpURLConnection = null;
    InputStream inputStream = null;
    try {
      // Keep opening connection until
      // Connect and download the file
      episodeURL = new URL(pmEpisode.getEnclosureURL());
      httpURLConnection = (HttpURLConnection) episodeURL.openConnection();
      int responseCode = httpURLConnection.getResponseCode();
      Log.d(LOG_TAG, "Response code: " + httpURLConnection.getResponseCode());

      while (responseCode != HttpURLConnection.HTTP_OK) {
        // Try to redirect or fail
        if ((responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
          responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
          responseCode == HttpURLConnection.HTTP_SEE_OTHER) &&
          (httpURLConnection.getHeaderField("Location")) != null) {
          episodeURL = new URL(httpURLConnection.getHeaderField("Location"));
          httpURLConnection = (HttpURLConnection) episodeURL.openConnection();
          responseCode = httpURLConnection.getResponseCode();
        } else {
          String errorMessage = getString(R.string.bad_http_response_code,
            responseCode);
          throw new IOException(errorMessage);
        }
      }

      Log.d(LOG_TAG, "Location: " + httpURLConnection.getHeaderField("Location"));


      inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
      // OutputStream output = new FileOutputStream("/sdcard/BarcodeScanner-debug.apk");
      int fileLength = httpURLConnection.getContentLength();
      Log.d(LOG_TAG, "The length of the file is:" + fileLength);
      byte data[] = new byte[1024];
      long total = 0;
      int count;

      while ((count = inputStream.read(data)) != -1) {
        total += count;
        // publishing the progress....
        // Bundle resultData = new Bundle();
        // resultData.putInt("progress" ,(int) (total * 100 / fileLength));
        // receiver.send(UPDATE_PROGRESS, resultData);
        // output.write(data, 0, count);
        // Log.d(LOG_TAG, "Bytes to string: " + new String(data));

        if (fileLength > 0) {
          float percentageComplete = ((((float)total) / ((float)fileLength)) * 100.0f);
          String percentageCompleteString = String.format(Locale.ENGLISH, "%.2f",
            percentageComplete);
          updateIntent.putExtra(INTENT_EXTRA_KEY_UPDATE_MESSAGE,
            getResources().getString(R.string.episode_download_progress_dialog_known_percentage,
              percentageCompleteString));
          Log.d(LOG_TAG, "Downloaded: " + percentageCompleteString + "%");
        } else {
          updateIntent.putExtra(INTENT_EXTRA_KEY_UPDATE_MESSAGE,
            getResources().getString(R.string.episode_download_progress_dialog_unknown_percentage,
              "" + total + " bytes"));
          Log.d(LOG_TAG, "Downloaded: " + total);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
      }
//      output.flush();
//      output.close();
      Log.d(LOG_TAG, "DONE! Downloaded: " + total + " fileLength: " + fileLength);
      inputStream.close();
    } catch (IOException ioException) {
      finishedIntent.putExtra(INTENT_EXTRA_KEY_ERROR_CODE,
        DownloadEpisodeExceptionCodes.DATA_RETRIEVAL_FAILED);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_FINISHED_SUCCESS, false);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_DETAILED_ERROR_MESSAGE,
        ioException.getMessage());

      return;
    }
    finishedIntent.putExtra(INTENT_EXTRA_KEY_FINISHED_SUCCESS, true);
    LocalBroadcastManager.getInstance(this).sendBroadcast(finishedIntent);
  }


  @Override
  public void onDestroy() {
    super.onDestroy();
  }
}
