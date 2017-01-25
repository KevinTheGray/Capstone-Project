package udacity.kevin.podcastmaster.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
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

public class EpisodeListCursorAdapter
  extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {

  private final int VIEW_TYPE_CHANNEL_DETAIL = 0;
  private final int VIEW_TYPE_EPISODE = 1;

  private Context mContext;
  private PMChannel mPMChannel;

  public EpisodeListCursorAdapter(Context context, Cursor cursor, PMChannel pmChannel){
    super(context, cursor);
    mPMChannel = pmChannel;
    mContext = context;
  }

  @Override
  public int getItemCount() {
    // Plus one for the header
    return (super.getItemCount() + 1);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
    if (position == 0) {
      onBindViewHolder(viewHolder, null);
    } else if (getCursor().moveToPosition(position - 1)) {
      onBindViewHolder(viewHolder, getCursor());
    } else {
      throw new IllegalStateException("couldn't move cursor to position " + position);
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_CHANNEL_DETAIL) {
      View cellView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.cell_channel_detail, parent, false);
      return new EpisodeListCursorAdapter.ChannelDetailViewHolder(cellView);
    } else {
      View cellView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.cell_episode, parent, false);
      return new EpisodeListCursorAdapter.EpisodeViewHolder(cellView);
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
    if (cursor == null) {
      ChannelDetailViewHolder channelDetailViewHolder = (ChannelDetailViewHolder) viewHolder;
      Glide.with(mContext).load(mPMChannel.getImageURL()).into(channelDetailViewHolder.channelImage);
      channelDetailViewHolder.channelTitle.setText(mPMChannel.getTitle());
      if (mPMChannel.getDescription() != null) {
        channelDetailViewHolder.channelDescription
          .setText(Html.fromHtml(mPMChannel.getDescription()).toString());
      } else {
        channelDetailViewHolder.channelDescription.setVisibility(View.GONE);
      }
    } else {
      PMEpisode pmEpisode = new PMEpisode(cursor);
      EpisodeViewHolder episodeViewHolder = (EpisodeViewHolder) viewHolder;
      episodeViewHolder.episodeTitle.setText(pmEpisode.getTitle());
      episodeViewHolder.episodeDescription
        .setText(Html.fromHtml(pmEpisode.getDescription()).toString());
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (position == 0) {
      return VIEW_TYPE_CHANNEL_DETAIL;
    } else {
      return VIEW_TYPE_EPISODE;
    }
  }

  private static class ChannelDetailViewHolder extends RecyclerView.ViewHolder {
    final ImageView channelImage;
    final TextView channelTitle;
    final TextView channelDescription;
    ChannelDetailViewHolder(View cellView) {
      super(cellView);
      channelImage = (ImageView) cellView.findViewById(R.id.image_view_channel);
      channelTitle = (TextView) cellView.findViewById(R.id.text_view_channel_title);
      channelDescription = (TextView) cellView.findViewById(R.id.text_view_channel_description);
    }
  }

  private static class EpisodeViewHolder extends RecyclerView.ViewHolder {;
    final TextView episodeTitle;
    final TextView episodeDescription;
    EpisodeViewHolder(View cellView) {
      super(cellView);
      episodeTitle = (TextView) cellView.findViewById(R.id.text_view_episode_title);
      episodeDescription = (TextView) cellView.findViewById(R.id.text_view_episode_description);
    }
  }
}
