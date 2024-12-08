import scrapetube
import sys

channel_url = sys.argv[1]

videos = scrapetube.get_channel(None, channel_url, None, 5, 0.01)

for video in videos:
    print(video["videoId"])
    print(video["viewCountText"]["simpleText"], file=open('python_script_channel_videos_output.txt', 'a'))
    print(video["viewCountText"]["simpleText"])
    print(video["title"]["runs"][0]["text"], file=open('python_script_channel_videos_output.txt', 'a'))
    print(video["title"]["runs"][0]["text"])