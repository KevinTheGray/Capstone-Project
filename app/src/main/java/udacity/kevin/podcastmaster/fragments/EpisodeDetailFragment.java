package udacity.kevin.podcastmaster.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.activities.MainActivity;
import udacity.kevin.podcastmaster.data.PodcastCRUDHelper;
import udacity.kevin.podcastmaster.listeners.DownloadRequestListener;
import udacity.kevin.podcastmaster.models.PMChannel;
import udacity.kevin.podcastmaster.models.PMEpisode;
import udacity.kevin.podcastmaster.networking.downloadcontent.DownloadEpisodeReceiver;
import udacity.kevin.podcastmaster.networking.downloadcontent.DownloadEpisodeService;

public class EpisodeDetailFragment extends Fragment implements DownloadRequestListener,
  DownloadEpisodeReceiver.DownloadEpisodeReceiverCallback {
  public static final String FRAGMENT_TAG = "EpisodeDetailFragment";
  public static final String LOG_TAG = "EpisodeDetailFragment";
  public static final String BUNDLE_KEY_EPISODE_PARCELABLE = "BUNDLE_KEY_PM_EPISODE";
  public static final String BUNDLE_KEY_CHANNEL_PARCELABLE = "BUNDLE_KEY_PM_CHANNEL";
  private DownloadEpisodeReceiver mDownloadEpisodeReceiver;
  private PMEpisode mPMEpisode;
  private PMChannel mPMChannel;
  private Button mDownloadButton;
  private LinearLayout mDownloadedButtonBar;
  private TextView mDownloadDetailMessage;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPMEpisode = getArguments().getParcelable(BUNDLE_KEY_EPISODE_PARCELABLE);
    mPMChannel = getArguments().getParcelable(BUNDLE_KEY_CHANNEL_PARCELABLE);

    IntentFilter downloadRSSFeedIntentFilter = new IntentFilter();
    downloadRSSFeedIntentFilter.addAction(DownloadEpisodeService.BROADCAST_UPDATE_ACTION);
    downloadRSSFeedIntentFilter.addAction(DownloadEpisodeService.BROADCAST_FINISHED_ACTION);

    mDownloadEpisodeReceiver = new DownloadEpisodeReceiver();
    LocalBroadcastManager.getInstance(getContext()).registerReceiver(mDownloadEpisodeReceiver,
      downloadRSSFeedIntentFilter);
    mDownloadEpisodeReceiver.setCallback(this);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_episode_detail, container, false);
    ImageView imageView = (ImageView) rootView.findViewById(R.id.image_view_channel);
    TextView episodeTitleTextView =
      (TextView) rootView.findViewById(R.id.text_view_episode_title);
    TextView episodeDescriptionTextView =
      (TextView) rootView.findViewById(R.id.text_view_episode_description);
    TextView episodePubDateTextView =
      (TextView) rootView.findViewById(R.id.text_view_episode_pub_date);
    TextView episodeDurationTextView =
      (TextView) rootView.findViewById(R.id.text_view_episode_duration);

    mDownloadButton = (Button) rootView.findViewById(R.id.button_download);
    mDownloadedButtonBar = (LinearLayout)
      rootView.findViewById(R.id.linear_layout_downloaded_button_bar);
    mDownloadDetailMessage = (TextView) rootView.findViewById(R.id.text_view_download_message);

    Glide.with(getActivity()).load(mPMChannel.getImageURL()).into(imageView);
    episodeTitleTextView.setText(Html.fromHtml(mPMEpisode.getTitle()).toString());
    episodePubDateTextView.setText(mPMEpisode.getPubDate());
    if (mPMEpisode.getDuration() != null) {
      episodeDurationTextView.setText(mPMEpisode.getDuration());
    } else {
      episodeDurationTextView.setVisibility(View.GONE);
    }
    if (mPMEpisode.getDescription() != null) {
      episodeDescriptionTextView.setText(Html.fromHtml(mPMEpisode.getDescription()).toString());
    } else {
      episodeDescriptionTextView.setVisibility(View.GONE);
    }

    mDownloadButton.setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          onDownloadNowButtonClicked(view);
        }
      }
    );
    rootView.findViewById(R.id.button_delete).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onDeleteButtonClicked(view);
      }
    });
    rootView.findViewById(R.id.button_play).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onPlayButtonClicked(view);
      }
    });

    layoutDownloadInformation(DownloadEpisodeService.currentlyDownloadingEpisode, null);
    return rootView;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mDownloadEpisodeReceiver.setCallback(null);
    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mDownloadEpisodeReceiver);
  }

  public void onDownloadNowButtonClicked(View v) {
    MainActivity mainActivity = (MainActivity) getActivity();
    mainActivity.showAd(this);
  }

  public void onDeleteButtonClicked(View v) {
    PodcastCRUDHelper podcastCRUDHelper = new PodcastCRUDHelper(getContext().getContentResolver());
    PMEpisode pmEpisode = podcastCRUDHelper.deletePMEpisodeDownload(getContext(), mPMEpisode);
    if (pmEpisode != null) {
      mPMEpisode = pmEpisode;
      layoutDownloadInformation(DownloadEpisodeService.currentlyDownloadingEpisode, null);
    }
  }

  public void onPlayButtonClicked(View v) {
  }

  @Override
  public void onBeginDownload() {
    Intent intent = new Intent(getActivity(), DownloadEpisodeService.class);
    intent.putExtra(DownloadEpisodeService.INTENT_EXTRA_KEY_PM_EPISODE, mPMEpisode);
    getActivity().startService(intent);
    DownloadEpisodeService.currentlyDownloadingEpisode = mPMEpisode;
    DownloadEpisodeService.currentlyDownloadingMessage =
      getString(R.string.episode_download_progress_dialog_start);
    layoutDownloadInformation(DownloadEpisodeService.currentlyDownloadingEpisode, null);
  }

  @Override
  public void onDownloadEpisodeIntentReceived(Context context, Intent intent) {
    if (intent.getAction().equals(DownloadEpisodeService.BROADCAST_FINISHED_ACTION)) {
      boolean success =
        intent.getBooleanExtra(DownloadEpisodeService.INTENT_EXTRA_KEY_FINISHED_SUCCESS, false);
      if (success) {
        mPMEpisode = intent.getParcelableExtra(
          DownloadEpisodeService.INTENT_EXTRA_KEY_UPDATED_EPISODE);
      }
    }
    layoutDownloadInformation(DownloadEpisodeService.currentlyDownloadingEpisode, null);
  }

  private void layoutDownloadInformation(@Nullable PMEpisode currentlyDownloadingEpisode,
                                         @Nullable String detailMessage) {

    // Check if it's downloaded
    if (mPMEpisode.getDownloadedMediaFilename() != null) {
      mDownloadButton.setVisibility(View.GONE);
      mDownloadDetailMessage.setVisibility(View.GONE);
      mDownloadedButtonBar.setVisibility(View.VISIBLE);
    } else {
      mDownloadButton.setVisibility(View.VISIBLE);
      mDownloadedButtonBar.setVisibility(View.GONE);
      if (currentlyDownloadingEpisode != null) {
        mDownloadDetailMessage.setVisibility(View.VISIBLE);
        mDownloadButton.setEnabled(false);
        if (currentlyDownloadingEpisode.getID() == mPMEpisode.getID()) {
          if (detailMessage == null) {
            mDownloadDetailMessage.setText(DownloadEpisodeService.currentlyDownloadingMessage);
          } else {
            mDownloadDetailMessage.setText(DownloadEpisodeService.currentlyDownloadingMessage);
          }
        } else {
          mDownloadDetailMessage.setText(getString(R.string.episode_download_only_one_allowed,
            currentlyDownloadingEpisode.getTitle()));
        }
      } else {
        mDownloadDetailMessage.setVisibility(View.GONE);
        mDownloadButton.setEnabled(true);
      }
    }
  }
}
