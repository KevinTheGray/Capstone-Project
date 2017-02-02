package udacity.kevin.podcastmaster.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.activities.MainActivity;
import udacity.kevin.podcastmaster.adapters.DownloadListCursorAdapter;
import udacity.kevin.podcastmaster.data.PodcastContract;
import udacity.kevin.podcastmaster.listeners.RecyclerViewItemClickListener;
import udacity.kevin.podcastmaster.models.PMChannel;
import udacity.kevin.podcastmaster.models.PMEpisode;

public class DownloadListFragment extends Fragment
	implements LoaderManager.LoaderCallbacks<Cursor> {

	private final String LOG_TAG = "DownloadListFragment";
	private final int LOADER_ID_DOWNLOADS = 0;
	private final int LOADER_ID_CHANNELS = 1;
	public static final String FRAGMENT_TAG = "EpisodeListFragment";
	private Cursor mCurrentDownloadListCursor;
	private DownloadListCursorAdapter mDownloadListCursorAdapter;
	private LongSparseArray<PMChannel> mChannelLongSparseArray = new LongSparseArray<>();
	private View mEmptyView;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDownloadListCursorAdapter = new DownloadListCursorAdapter(getActivity(), null,
			mChannelLongSparseArray);
		getLoaderManager().initLoader(LOADER_ID_CHANNELS, null, this);
		getLoaderManager().initLoader(LOADER_ID_DOWNLOADS, null, this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_downloads_list, container, false);

		RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.downloads_recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(mDownloadListCursorAdapter);

		recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(),
			new RecyclerViewItemClickListener.OnItemClickListener() {
				@Override
				public void onItemClick(View v, int position) {
					mCurrentDownloadListCursor.moveToPosition(position);
					PMEpisode pmEpisode = new PMEpisode(mCurrentDownloadListCursor);
					PMChannel pmChannel = mChannelLongSparseArray.get(pmEpisode.getChannelID());
					MainActivity mainActivity = (MainActivity) getActivity();
					mainActivity.episodeSelected(pmEpisode, pmChannel, false);
				}
			}));

		mEmptyView = rootView.findViewById(R.id.empty_view);

		return rootView;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == LOADER_ID_DOWNLOADS) {
			return new CursorLoader(getActivity(), PodcastContract.EpisodeEntry.CONTENT_URI, new String[]{
				PodcastContract.EpisodeEntry._ID,
				PodcastContract.EpisodeEntry.COLUMN_CHANNEL_ID,
				PodcastContract.EpisodeEntry.COLUMN_DESCRIPTION,
				PodcastContract.EpisodeEntry.COLUMN_TITLE,
				PodcastContract.EpisodeEntry.COLUMN_DOWNLOADED_MEDIA_URI,
				PodcastContract.EpisodeEntry.COLUMN_DURATION,
				PodcastContract.EpisodeEntry.COLUMN_ENCLOSURE_URL,
				PodcastContract.EpisodeEntry.COLUMN_GUID,
				PodcastContract.EpisodeEntry.COLUMN_PUB_DATE,},
				PodcastContract.EpisodeEntry.COLUMN_DOWNLOADED_MEDIA_URI + " IS NOT NULL",
				null,
				null);
		} else {
			return new CursorLoader(getActivity(), PodcastContract.ChannelEntry.CONTENT_URI, new String[]{
				PodcastContract.ChannelEntry._ID,
				PodcastContract.ChannelEntry.COLUMN_TITLE,
				PodcastContract.ChannelEntry.COLUMN_DESCRIPTION,
				PodcastContract.ChannelEntry.COLUMN_IMAGE_URL},
				null,
				null,
				null);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (loader.getId() == LOADER_ID_DOWNLOADS) {
			mCurrentDownloadListCursor = data;
			mDownloadListCursorAdapter.swapCursor(mCurrentDownloadListCursor);
			updateEmptyView();
		} else {
			mChannelLongSparseArray.clear();
			data.moveToFirst();
			while (!data.isAfterLast()) {
				PMChannel pmChannel = new PMChannel(data);
				mChannelLongSparseArray.append(pmChannel.getID(), pmChannel);
				data.moveToNext();
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(LOG_TAG, "Loader reset");
	}

	private void updateEmptyView() {
		if (mCurrentDownloadListCursor == null || mCurrentDownloadListCursor.getCount() == 0) {
			mEmptyView.setVisibility(View.VISIBLE);
		} else {
			mEmptyView.setVisibility(View.GONE);
		}
	}
}
