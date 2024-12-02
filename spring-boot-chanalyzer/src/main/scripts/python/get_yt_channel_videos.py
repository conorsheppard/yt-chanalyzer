import scrapetube
import sys

print('In get_yt_channel_videos.py 1 ...', file=open('output.txt', 'a'))

channel_id = sys.argv[1]

videos = scrapetube.get_channel(channel_id, None, None, 5, 0.01)

print('In get_yt_channel_videos.py 2 ...', file=open('output.txt', 'a'))

for video in videos:
    print(video["videoId"], file=open('python_script_channel_videos_output.txt', 'a'))
    print(video["videoId"])
    print(video["viewCountText"]["simpleText"], file=open('python_script_channel_videos_output.txt', 'a'))
    print(video["viewCountText"]["simpleText"])
    print(video["title"]["runs"][0]["text"], file=open('python_script_channel_videos_output.txt', 'a'))
    print(video["title"]["runs"][0]["text"])