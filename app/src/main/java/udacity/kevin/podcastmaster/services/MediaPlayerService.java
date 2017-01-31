package udacity.kevin.podcastmaster.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import java.io.File;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
	MediaPlayer.OnErrorListener {

	public final static String ACTION_PLAY = "udacity.kevin.podcastmaster.ACTION_PLAY";
	public final static String INTENT_EXTRA_KEY_FILENAME = "udacity.kevin.podcastmaster.KEY_FILENAME";

	public final static String ACTION_PAUSE = "udacity.kevin.podcastmaster.ACTION_PAUSE";
	public final static String ACTION_SEEK = "udacity.kevin.podcastmaster.ACTION_SEEK";

	private MediaPlayer mMediaPlayer;
	private String mCurrentluPlayingFilename;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String filename = intent.getStringExtra(INTENT_EXTRA_KEY_FILENAME);
			if (intent.getAction().equals(ACTION_PLAY)) {
				if (filename == null || filename.equals(mCurrentluPlayingFilename)) {
					if (mMediaPlayer != null) {
						if (mMediaPlayer.isPlaying()) {
							// mMediaPlayer.pause();
							mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + 1500);
						} else {
							mMediaPlayer.start();
						}
					}
				} else {
					if (mMediaPlayer != null) {
						mMediaPlayer.stop();
						mMediaPlayer.release();
						mMediaPlayer = null;
						mCurrentluPlayingFilename = null;
					}
					File file = new File(getFilesDir(), filename);
					Uri uri = Uri.fromFile(file);
					mMediaPlayer = MediaPlayer.create(this, uri);
					mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
					mCurrentluPlayingFilename = filename;
					if (mMediaPlayer != null) {
						mMediaPlayer.setOnPreparedListener(this);
						mMediaPlayer.start();
					}
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		mediaPlayer.start();
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
		return false;
	}
}
