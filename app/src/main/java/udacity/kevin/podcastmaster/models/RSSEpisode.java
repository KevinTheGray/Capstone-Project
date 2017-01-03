package udacity.kevin.podcastmaster.models;

import android.content.Context;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.exceptions.InvalidModelException;

public class RSSEpisode {
  private String title;
  private String pubDate;
  private String description;
  private String duration;
  private String enclosureURL;

  public RSSEpisode(String title, String pubDate, String description, String duration,
                    String enclosureURL, Context context) throws InvalidModelException {
    if (title == null || pubDate == null || enclosureURL == null) {
      throw new InvalidModelException(context.getString(R.string.bad_episode_model_exception,
        title, pubDate, enclosureURL));
    }
    this.title = title;
    this.pubDate = pubDate;
    this.description = description;
    this.duration = duration;
    this.enclosureURL = enclosureURL;
  }

  public String getTitle() {
    return title;
  }

  public String getPubDate() {
    return pubDate;
  }

  public String getDescription() {
    return description;
  }

  public String getDuration() {
    return duration;
  }

  public String getEnclosureURL() {
    return enclosureURL;
  }
}
