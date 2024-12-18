from flask import Flask
from flask import request
import scrapetube
# from gevent.pywsgi import WSGIServer

app = Flask(__name__)

@app.route("/scrape", methods=['GET'])
def scrape():
    channel_url = request.args.get('channelUrl')
    numVideos = int(request.args.get('numVideos'))
    print(channel_url)
    print(numVideos)
    # videos = scrapetube.get_channel(None, channel_url, None, numVideos, 0.01)
    videos = scrapetube.get_channel(None, channel_url, None, numVideos, 0.01)
    response = []

    for video in videos:
        obj = {}
        video_id = video["videoId"]
        obj['videoId'] = video_id
        video_data = scrapetube.get_video(video_id)
        obj['title'] = video["title"]["runs"][0]["text"]
        obj['uploadDate'] = video_data["dateText"]["simpleText"]
        obj['viewCount'] = video["viewCountText"]["simpleText"]
        response.append(obj)

    return response

if __name__ == '__main__':
#     # Debug/Development
    app.run(debug=True, host="0.0.0.0", port="5050")
#     # Production
#     http_server = WSGIServer(("127.0.0.1", 5050), app)
#     http_server.serve_forever()