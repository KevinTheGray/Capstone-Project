package udacity.kevin.podcastmaster.networking.downloadcontent;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.data.PodcastCRUDHelper;
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
  public final static String INTENT_EXTRA_KEY_UPDATED_EPISODE =
    "udacity.kevin.podcastmaster.downloadepisodeservice.UPDATED_EPISODE";
  public final static String INTENT_EXTRA_KEY_SUCCESS_MESSAGE =
    "udacity.kevin.podcastmaster.downloadepisodeservice.SUCCESS_MESSAGE";

  public static PMEpisode currentlyDownloadingEpisode;
  public static String currentlyDownloadingMessage;

  public final String LOG_TAG = "DownloadEpisodeService";

  public DownloadEpisodeService() {
    super("DownloadEpisodeService");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    currentlyDownloadingEpisode = intent.getParcelableExtra(INTENT_EXTRA_KEY_PM_EPISODE);
    currentlyDownloadingMessage = getString(R.string.episode_download_progress_dialog_start);
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Intent finishedIntent = new Intent(BROADCAST_FINISHED_ACTION);
    Intent updateIntent = new Intent(BROADCAST_UPDATE_ACTION);
    String filename = currentlyDownloadingEpisode.getGuid().replaceAll("\\s+","");
    filename = filename.replace("\\", "");
    filename = filename.replace("/", "");
    String[] segments = currentlyDownloadingEpisode.getEnclosureURL().split("/");
    filename = segments[segments.length-1];
    boolean fileDownloadComplete = false;

    URL episodeURL = null;
    HttpURLConnection httpURLConnection = null;
    InputStream inputStream = null;
    try {
      // Keep opening connection until
      // Connect and download the file
      episodeURL = new URL(currentlyDownloadingEpisode.getEnclosureURL());
      httpURLConnection = (HttpURLConnection) episodeURL.openConnection();
      int responseCode = httpURLConnection.getResponseCode();

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

      inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
      FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
      int fileLength = httpURLConnection.getContentLength();
      byte data[] = new byte[1024];
      long total = 0;
      int count;

      while ((count = inputStream.read(data)) != -1) {
        total += count;
        outputStream.write(data, 0, count);

        if (fileLength > 0) {
          float percentageComplete = ((((float)total) / ((float)fileLength)) * 100.0f);
          String percentageCompleteString = String.format(Locale.ENGLISH, "%.2f",
            percentageComplete);
          Log.d(LOG_TAG, percentageCompleteString);
          currentlyDownloadingMessage = getResources()
            .getString(R.string.episode_download_progress_dialog_known_percentage,
              percentageCompleteString);
          updateIntent.putExtra(INTENT_EXTRA_KEY_UPDATE_MESSAGE, currentlyDownloadingMessage);
        } else {
          currentlyDownloadingMessage = getResources()
            .getString(R.string.episode_download_progress_dialog_unknown_percentage, "" + total);
          updateIntent.putExtra(INTENT_EXTRA_KEY_UPDATE_MESSAGE, currentlyDownloadingMessage);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
      }
      outputStream.flush();
      outputStream.close();
      inputStream.close();
    } catch (IOException ioException) {
      Log.e(LOG_TAG, ioException.getMessage());
      finishedIntent.putExtra(INTENT_EXTRA_KEY_ERROR_CODE,
        DownloadEpisodeExceptionCodes.DATA_RETRIEVAL_FAILED);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_FINISHED_SUCCESS, false);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_DETAILED_ERROR_MESSAGE,
        ioException.getMessage());
      File file = new File(getFilesDir(), filename);
      if (file.exists()) {
        boolean fileDeleted = file.delete();
        if (!fileDeleted) {
          Log.e(LOG_TAG, "There was an error downloading the file and it wasn't deleted");
        }
      }
      LocalBroadcastManager.getInstance(this).sendBroadcast(finishedIntent);
      return;
    }
    PodcastCRUDHelper podcastCRUDHelper = new PodcastCRUDHelper(getContentResolver());
    PMEpisode updatedPMEpisode = podcastCRUDHelper.updatedDownloadedEpisode(
      currentlyDownloadingEpisode, filename);
    if (updatedPMEpisode == null) {
      File file = new File(getFilesDir(), filename);
      if (file.exists()) {
        boolean fileDeleted = file.delete();
        if (!fileDeleted) {
          Log.e(LOG_TAG, "There was an error downloading the file and it wasn't deleted");
        }
      }
      currentlyDownloadingEpisode = null;
      finishedIntent.putExtra(INTENT_EXTRA_KEY_FINISHED_SUCCESS, false);
    } else {
      currentlyDownloadingEpisode = updatedPMEpisode;
      finishedIntent.putExtra(INTENT_EXTRA_KEY_UPDATED_EPISODE, updatedPMEpisode);
      finishedIntent.putExtra(INTENT_EXTRA_KEY_FINISHED_SUCCESS, true);
    }
    LocalBroadcastManager.getInstance(this).sendBroadcast(finishedIntent);
  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    currentlyDownloadingEpisode = null;
    currentlyDownloadingMessage = null;
  }
}
