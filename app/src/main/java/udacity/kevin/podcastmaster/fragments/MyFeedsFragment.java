package udacity.kevin.podcastmaster.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.exceptions.ErrorMessageFactory;
import udacity.kevin.podcastmaster.networking.downloadrssfeed.DownloadRSSFeedReceiver;
import udacity.kevin.podcastmaster.networking.downloadrssfeed.DownloadRSSFeedService;

public class MyFeedsFragment extends Fragment implements
  DownloadRSSFeedReceiver.DownloadRSSFeedReceiverCallback {

  public static final String FRAGMENT_TAG = "MyFeedsFragment";
  private final String LOG_TAG = "MyFeedsFragment";
  private DownloadRSSFeedReceiver mDownloadRSSFeedReceiver;

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
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_my_feeds, container, false);

    FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
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
            }
          }).show();
      }
    });

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
    }
  }
}
