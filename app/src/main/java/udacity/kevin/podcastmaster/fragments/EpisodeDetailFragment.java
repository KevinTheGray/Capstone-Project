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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.activities.MainActivity;
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

    rootView.findViewById(R.id.button_download).setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          onDownloadNowButtonClicked(view);
        }
      }
    );
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

  @Override
  public void onBeginDownload() {
    Intent intent = new Intent(getActivity(), DownloadEpisodeService.class);
    intent.putExtra(DownloadEpisodeService.INTENT_EXTRA_KEY_PM_EPISODE, mPMEpisode);
    getActivity().startService(intent);
  }

  @Override
  public void onDownloadEpisodeIntentReceived(Context context, Intent intent) {
  }
}
