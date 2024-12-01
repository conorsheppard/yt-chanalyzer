import scrapetube
import sys

video_id = sys.argv[1]

video_data = scrapetube.get_video(video_id)

print(video_data["dateText"]["simpleText"])