package udacity.kevin.podcastmaster.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.util.LongSparseArray;
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

public class DownloadListCursorAdapter
	extends CursorRecyclerViewAdapter<DownloadListCursorAdapter.ViewHolder> {

	private Context mContext;
	private LongSparseArray<PMChannel> mChannelLongSparseArray = new LongSparseArray<>();

	public DownloadListCursorAdapter(Context context, Cursor cursor,
																	 LongSparseArray<PMChannel> channelLongSparseArray) {
		super(context, cursor);
		mContext = context;
		mChannelLongSparseArray = channelLongSparseArray;
	}

	@Override
	public DownloadListCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View cellView = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.cell_download, parent, false);
		return new DownloadListCursorAdapter.ViewHolder(cellView);
	}

	@Override
	public void onBindViewHolder(DownloadListCursorAdapter.ViewHolder viewHolder, Cursor cursor) {
		PMEpisode pmEpisode = new PMEpisode(cursor);
		PMChannel pmChannel = mChannelLongSparseArray.get(pmEpisode.getChannelID(), null);
		viewHolder.episodeTitle.setText(pmEpisode.getTitle());
		viewHolder.episodeDescription
			.setText(Html.fromHtml(pmEpisode.getDescription()).toString());
		if (pmChannel != null) {
			if (pmChannel.getImageURL() != null) {
				Glide.with(mContext).load(pmChannel.getImageURL()).into(viewHolder.channelImage);
			}
		}
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		final TextView episodeTitle;
		final TextView episodeDescription;
		final ImageView channelImage;

		ViewHolder(View cellView) {
			super(cellView);
			episodeTitle = (TextView) cellView.findViewById(R.id.text_view_episode_title);
			episodeDescription = (TextView) cellView.findViewById(R.id.text_view_episode_description);
			channelImage = (ImageView) cellView.findViewById(R.id.image_view_channel);
		}
	}
}
