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

public class ChannelCursorAdapter
  extends CursorRecyclerViewAdapter<ChannelCursorAdapter.ViewHolder> {

  private Context mContext;

  public ChannelCursorAdapter(Context context, Cursor cursor){
    super(context, cursor);
    mContext = context;
  }

  @Override
  public ChannelCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View cellView = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.cell_channel, parent, false);
    return new ViewHolder(cellView);
  }

  @Override
  public void onBindViewHolder(ChannelCursorAdapter.ViewHolder viewHolder, Cursor cursor) {
    PMChannel pmChannel = new PMChannel(cursor);
    viewHolder.channelTitle.setText(pmChannel.getTitle());
    if (pmChannel.getImageURL() != null) {
      Glide.with(mContext).load(pmChannel.getImageURL()).into(viewHolder.channelImage);
    }
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView channelTitle;
    final ImageView channelImage;

    ViewHolder(View cellView) {
      super(cellView);
      channelTitle = (TextView) cellView.findViewById(R.id.text_view_channel_title);
      channelImage = (ImageView) cellView.findViewById(R.id.image_view_channel);
    }
  }
}
