package udacity.kevin.podcastmaster.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.models.PMEpisode;

public class DownloadListCursorAdapter
	extends CursorRecyclerViewAdapter<DownloadListCursorAdapter.ViewHolder> {

	private Context mContext;

	public DownloadListCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		mContext = context;
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
		DownloadListCursorAdapter.ViewHolder episodeViewHolder =
			(DownloadListCursorAdapter.ViewHolder) viewHolder;
		episodeViewHolder.episodeTitle.setText(pmEpisode.getTitle());
		episodeViewHolder.episodeDescription
			.setText(Html.fromHtml(pmEpisode.getDescription()).toString());
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		final TextView episodeTitle;
		final TextView episodeDescription;

		ViewHolder(View cellView) {
			super(cellView);
			episodeTitle = (TextView) cellView.findViewById(R.id.text_view_episode_title);
			episodeDescription = (TextView) cellView.findViewById(R.id.text_view_episode_description);
		}
	}
}
