### API

#### Accepts
_*GET*_ `api/v1/channels/{channelName}/videos`

#### Response
Produces: `text/event-stream`

```json
{
  "views": "30000",
  "publishedTime": "Jun 1, 2025"
}
```
The system consumes a stream of these YouTubeVideoDTO objects from the downstream scraper service and passes them back to the React front end client.

#### Database schema

```sql
CREATE TABLE scraped_videos (
    id SERIAL PRIMARY KEY,
    channel_name TEXT NOT NULL,
    video_id TEXT NOT NULL, -- YouTube ID
    video_title TEXT NOT NULL,
    month_label TEXT NOT NULL,
    views INTEGER,
    scraped_at TIMESTAMP NOT NULL,
    published_date DATE,
    scraped_date DATE,
    CONSTRAINT unique_channel_scrape UNIQUE (channel_name, video_id, scraped_date)
);
```

```sql
CREATE TABLE scrape_status (
    id SERIAL PRIMARY KEY,
    channel_name TEXT NOT NULL,
    scraped_date DATE NOT NULL,
    status TEXT NOT NULL, -- "in_progress", "completed", "failed"
    started_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(channel_name, scraped_date)
);
```