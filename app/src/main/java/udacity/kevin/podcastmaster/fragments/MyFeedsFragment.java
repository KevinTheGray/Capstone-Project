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
              Log.d(LOG_TAG, input.toString());
              Intent intent = new Intent(getActivity(), DownloadRSSFeedService.class);
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
  }
}
