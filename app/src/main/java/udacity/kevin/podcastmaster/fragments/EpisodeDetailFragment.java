package udacity.kevin.podcastmaster.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.models.PMChannel;
import udacity.kevin.podcastmaster.models.PMEpisode;

public class EpisodeDetailFragment extends Fragment {
  public static final String FRAGMENT_TAG = "EpisodeDetailFragment";
  public static final String LOG_TAG = "EpisodeDetailFragment";
  public static final String BUNDLE_KEY_EPISODE_PARCELABLE = "BUNDLE_KEY_PM_EPISODE";
  public static final String BUNDLE_KEY_CHANNEL_PARCELABLE = "BUNDLE_KEY_PM_CHANNEL";
  private PMEpisode mPMEpisode;
  private PMChannel mPMChannel;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPMEpisode = getArguments().getParcelable(BUNDLE_KEY_EPISODE_PARCELABLE);
    mPMChannel = getArguments().getParcelable(BUNDLE_KEY_CHANNEL_PARCELABLE);
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
    return rootView;
  }

}
