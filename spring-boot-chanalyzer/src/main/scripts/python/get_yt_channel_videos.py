import scrapetube
import sys

channel_id = sys.argv[1]

videos = scrapetube.get_channel(channel_id, None, None, 5, 0.01)

# with open("python_script_channel_videos_output.txt", "a") as f:
#     print(videos, file=f)

for video in videos:
    print(video["videoId"])
    print(video["viewCountText"]["simpleText"])
    print(video["title"]["runs"][0]["text"])