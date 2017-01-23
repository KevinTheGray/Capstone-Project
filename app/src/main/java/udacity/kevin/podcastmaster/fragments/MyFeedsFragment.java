package udacity.kevin.podcastmaster.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.activities.MainActivity;
import udacity.kevin.podcastmaster.adapters.ChannelCursorAdapter;
import udacity.kevin.podcastmaster.data.PodcastCRUDHelper;
import udacity.kevin.podcastmaster.data.PodcastContract;
import udacity.kevin.podcastmaster.exceptions.ErrorMessageFactory;
import udacity.kevin.podcastmaster.listeners.RecyclerViewItemClickListener;
import udacity.kevin.podcastmaster.models.PMChannel;
import udacity.kevin.podcastmaster.networking.downloadrssfeed.DownloadRSSFeedReceiver;
import udacity.kevin.podcastmaster.networking.downloadrssfeed.DownloadRSSFeedService;

public class MyFeedsFragment extends Fragment implements
  DownloadRSSFeedReceiver.DownloadRSSFeedReceiverCallback, LoaderManager.LoaderCallbacks<Cursor> {

  public static final String FRAGMENT_TAG = "MyFeedsFragment";
  private final String LOG_TAG = "MyFeedsFragment";
  private DownloadRSSFeedReceiver mDownloadRSSFeedReceiver;
  private ProgressDialog mDownloadRSSFeedProgressDialog;
  private ChannelCursorAdapter mChannelCursorAdapter;
  private View mEmptyView;
  private Cursor mCurrentChannelCursor;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    IntentFilter downloadRSSFeedIntentFilter = new IntentFilter();
    downloadRSSFeedIntentFilter.addAction(DownloadRSSFeedService.BROADCAST_UPDATE_ACTION);
    downloadRSSFeedIntentFilter.addAction(DownloadRSSFeedService.BROADCAST_FINISHED_ACTION);

    mDownloadRSSFeedReceiver = new DownloadRSSFeedReceiver();
    LocalBroadcastManager.getInstance(getContext()).registerReceiver(
      mDownloadRSSFeedReceiver, downloadRSSFeedIntentFilter);
      mDownloadRSSFeedReceiver.setCallback(this);

    mChannelCursorAdapter = new ChannelCursorAdapter(getActivity(), null);
    getLoaderManager().initLoader(0, null, this);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_my_feeds, container, false);

    FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
    RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.channels_recycler_view);

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new MaterialDialog.Builder(getActivity())
          .title(getString(R.string.add_feed_dialog_title))
          .content(getString(R.string.add_feed_dialog_content))
          .negativeText(getString(R.string.add_feed_dialog_negative))
          .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
          .input(getString(R.string.add_feed_dialog_hint), null, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
              // Do something
              Intent intent = new Intent(getActivity(), DownloadRSSFeedService.class);
              intent.putExtra(DownloadRSSFeedService.INTENT_EXTRA_KEY_RSS_URL, input.toString());
              getActivity().startService(intent);
              mDownloadRSSFeedProgressDialog = ProgressDialog.show(getActivity(), "",
                getContext().getString(R.string.add_feed_progress_dialog_downloading), true);
            }
          }).show();
      }
    });

    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    recyclerView.setAdapter(mChannelCursorAdapter);
    ItemTouchHelper.SimpleCallback itemTouchHelperSimpleCallback =
      new ItemTouchHelper.SimpleCallback(ItemTouchHelper.START, ItemTouchHelper.START) {
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
          channelSwipedToDismiss(viewHolder.getAdapterPosition());
        }
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) { return false; }
      };
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperSimpleCallback);
    itemTouchHelper.attachToRecyclerView(recyclerView);

    recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(),
      new RecyclerViewItemClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
          mCurrentChannelCursor.moveToPosition(position);
          PMChannel pmChannel = new PMChannel(mCurrentChannelCursor);
          MainActivity mainActivity = (MainActivity) getActivity();
          mainActivity.channelSelected(pmChannel);
        }
      }));

    mEmptyView = rootView.findViewById(R.id.empty_view);

    return rootView;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mDownloadRSSFeedReceiver.setCallback(null);
    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mDownloadRSSFeedReceiver);
  }

  @Override
  public void onDownloadRSSFeedIntentReceived(Context context, Intent intent) {
    Log.d(LOG_TAG, intent.getAction());
    if (intent.getAction().equals(DownloadRSSFeedService.BROADCAST_FINISHED_ACTION)) {
      if (mDownloadRSSFeedProgressDialog != null) {
        mDownloadRSSFeedProgressDialog.dismiss();
      }
      boolean success = intent.getBooleanExtra(
        DownloadRSSFeedService.INTENT_EXTRA_KEY_FINISHED_SUCCESS, false);
      if (!success) {
        String errorMessage = ErrorMessageFactory.GenerateErrorMessage(getActivity(),
          intent.getIntExtra(DownloadRSSFeedService.INTENT_EXTRA_KEY_ERROR_CODE, -1),
          intent.getStringExtra(DownloadRSSFeedService.INTENT_EXTRA_KEY_DETAILED_ERROR_MESSAGE));
        new MaterialDialog.Builder(getActivity())
          .title(context.getString(R.string.add_feed_error_dialog_title))
          .content(errorMessage)
          .positiveText(context.getString(R.string.OK))
          .show();
      } else {
        String successMessage = intent
          .getStringExtra(DownloadRSSFeedService.INTENT_EXTRA_KEY_SUCCESS_MESSAGE);
        new MaterialDialog.Builder(getActivity())
          .title(context.getString(R.string.add_feed_success_dialog_title))
          .content(successMessage)
          .positiveText(context.getString(R.string.OK))
          .show();
        getLoaderManager().restartLoader(0, null, this);
      }
    } else if (intent.getAction().equals(DownloadRSSFeedService.BROADCAST_UPDATE_ACTION)) {
      if (mDownloadRSSFeedProgressDialog != null) {
        String updateMessage =
          intent.getStringExtra(DownloadRSSFeedService.INTENT_EXTRA_KEY_UPDATE_MESSAGE);
        mDownloadRSSFeedProgressDialog.setMessage(updateMessage);
      }
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(getActivity(), PodcastContract.ChannelEntry.CONTENT_URI, new String[] {
      PodcastContract.ChannelEntry._ID,
      PodcastContract.ChannelEntry.COLUMN_TITLE,
      PodcastContract.ChannelEntry.COLUMN_DESCRIPTION,
      PodcastContract.ChannelEntry.COLUMN_IMAGE_URL},
      null,
      null,
      null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mChannelCursorAdapter.swapCursor(data);
    mCurrentChannelCursor = data;
    updateEmptyView();
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
  }

  private void updateEmptyView() {
    if (mCurrentChannelCursor == null || mCurrentChannelCursor.getCount() == 0) {
      mEmptyView.setVisibility(View.VISIBLE);
    } else {
      mEmptyView.setVisibility(View.GONE);
    }
  }

  private void channelSwipedToDismiss(int cursorPosition) {
    mCurrentChannelCursor.moveToPosition(cursorPosition);
    final PMChannel pmChannel = new PMChannel(mCurrentChannelCursor);
    final LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = this;
    // Show modal requesting the deletion
    // if accepted, initiate delete and restart the cursor.
    new MaterialDialog.Builder(getActivity())
      .title(getString(R.string.remove_feed_dialog_title, pmChannel.getTitle()))
      .content(getString(R.string.remove_feed_dialog_content, pmChannel.getTitle()))
      .positiveText(getString(R.string.remove_feed_dialog_positive_text, pmChannel.getTitle()))
      .onPositive(new MaterialDialog.SingleButtonCallback() {
        @Override
        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
          PodcastCRUDHelper crudHelper = new PodcastCRUDHelper(getActivity().getContentResolver());
          crudHelper.deletePMChannel(pmChannel);
          getLoaderManager().restartLoader(0, null, loaderCallbacks);
        }
      })
      .negativeText(getString(R.string.remove_feed_dialog_negative_text))
      .onNegative(new MaterialDialog.SingleButtonCallback() {
        @Override
        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
          getLoaderManager().restartLoader(0, null, loaderCallbacks);
        }
      })
      .cancelable(false)
      .show();
  }
}
