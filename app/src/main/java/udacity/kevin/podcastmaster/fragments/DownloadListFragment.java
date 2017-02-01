package udacity.kevin.podcastmaster.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.data.PodcastContract;
import udacity.kevin.podcastmaster.listeners.RecyclerViewItemClickListener;

public class DownloadListFragment extends Fragment
	implements LoaderManager.LoaderCallbacks<Cursor> {

	private final String LOG_TAG = "DownloadListFragment";
	private Cursor mCurrentDownloadListCursor;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLoaderManager().initLoader(0, null, this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_episodes_list, container, false);

		RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.episodes_recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		// recyclerView.setAdapter(mEpisodeListCursorAdapter);

		recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(),
			new RecyclerViewItemClickListener.OnItemClickListener() {
				@Override
				public void onItemClick(View v, int position) {
//						int modifiedPosition = position - 1;
//						mCurrentEpisodeListCursor.moveToPosition(modifiedPosition);
//						PMEpisode pmEpisode = new PMEpisode(mCurrentEpisodeListCursor);
//						MainActivity mainActivity = (MainActivity) getActivity();
//						mainActivity.episodeSelected(pmEpisode, mPMChannel);
				}
			}));

		return rootView;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), PodcastContract.EpisodeEntry.CONTENT_URI, new String[] {
			PodcastContract.EpisodeEntry._ID,
			PodcastContract.EpisodeEntry.COLUMN_CHANNEL_ID,
			PodcastContract.EpisodeEntry.COLUMN_DESCRIPTION,
			PodcastContract.EpisodeEntry.COLUMN_TITLE,
			PodcastContract.EpisodeEntry.COLUMN_DOWNLOADED_MEDIA_URI,
			PodcastContract.EpisodeEntry.COLUMN_DURATION,
			PodcastContract.EpisodeEntry.COLUMN_ENCLOSURE_URL,
			PodcastContract.EpisodeEntry.COLUMN_GUID,
			PodcastContract.EpisodeEntry.COLUMN_PUB_DATE,},
			PodcastContract.EpisodeEntry.COLUMN_CHANNEL_ID + " = ?",
			null,
			null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mCurrentDownloadListCursor = data;
		Log.d(LOG_TAG, "" + mCurrentDownloadListCursor.getCount());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(LOG_TAG, "Loader reset");
	}
}
