package udacity.kevin.podcastmaster.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.data.PodcastContract;

public class EpisodeListFragment extends Fragment
  implements LoaderManager.LoaderCallbacks<Cursor> {
  public static final String FRAGMENT_TAG = "EpisodeListFragment";
  private View mEmptyView;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getLoaderManager().initLoader(0, null, this);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_episodes_list, container, false);

    mEmptyView = rootView.findViewById(R.id.empty_view);

    return rootView;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(getActivity(), PodcastContract.EpisodeEntry.CONTENT_URI, new String[] {
      PodcastContract.EpisodeEntry._ID,
      PodcastContract.EpisodeEntry.COLUMN_TITLE,
      PodcastContract.EpisodeEntry.COLUMN_DOWNLOADED_MEDIA_URI,
      PodcastContract.EpisodeEntry.COLUMN_DURATION,
      PodcastContract.EpisodeEntry.COLUMN_PUB_DATE,},
      null,
      null,
      null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    Log.d("BOOP", "" + data.getCount());
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {

  }

}
