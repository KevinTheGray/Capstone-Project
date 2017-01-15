package udacity.kevin.podcastmaster.models;

import android.content.Context;

import java.util.ArrayList;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.exceptions.InvalidModelException;

public class RSSChannel {
  private String title;
  private String description;
  private String imageURL;
  private String rssURL;
  private ArrayList<RSSEpisode> episodes;
  public RSSChannel(String title, String description, String summary, String imageURL,
                    String rssURL, ArrayList<RSSEpisode> episodes, Context context)
    throws InvalidModelException {
    if (title == null || rssURL == null) {
      throw new InvalidModelException(context.getString(R.string.bad_channel_model_exception,
        title, rssURL));
    }
    this.title = title;
    this.imageURL = imageURL;
    this.episodes = episodes;
    this.rssURL = rssURL;
    if (description != null) {
      this.description = description;
    } else if (summary != null) {
      this.description = summary;
    }
  }

  public String getRSSURL() {
    return rssURL;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getImageURL() {
    return imageURL;
  }

  public ArrayList<RSSEpisode> getEpisodes() {
    return episodes;
  }
}
