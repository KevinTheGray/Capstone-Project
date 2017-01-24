package udacity.kevin.podcastmaster.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.models.PMChannel;

public class EpisodeListCursorAdapter
  extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {

  private Context mContext;
  private PMChannel mPMChannel;

  public EpisodeListCursorAdapter(Context context, Cursor cursor, PMChannel pmChannel){
    super(context, cursor);
    mPMChannel = pmChannel;
    mContext = context;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View cellView = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.cell_channel_detail, parent, false);
    return new EpisodeListCursorAdapter.ChannelDetailViewHolder(cellView);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
    ChannelDetailViewHolder channelDetailViewHolder = (ChannelDetailViewHolder) viewHolder;
    Glide.with(mContext).load(mPMChannel.getImageURL()).into(channelDetailViewHolder.channelImage);
    channelDetailViewHolder.channelTitle.setText(mPMChannel.getTitle());
    if (mPMChannel.getDescription() != null) {
      channelDetailViewHolder.channelDescription.setText(mPMChannel.getDescription());
    } else {
      channelDetailViewHolder.channelDescription.setVisibility(View.GONE);
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
}
