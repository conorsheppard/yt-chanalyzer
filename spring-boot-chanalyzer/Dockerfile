FROM amazoncorretto:23-alpine-jdk
ARG JAR_FILE=target/chanalyzer-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} chanalyzer.jar

ARG SCRAPETUBE=src/main/scripts/python/scrapetube.py
COPY ${SCRAPETUBE} scrapetube.py
ARG CHANNEL_SCRIPT=src/main/scripts/python/get_yt_channel_videos.py
COPY ${CHANNEL_SCRIPT} get_yt_channel_videos.py
ARG VIDEO_SCRIPT=src/main/scripts/python/get_video_by_id.py
COPY ${VIDEO_SCRIPT} get_video_by_id.py

ENV PYTHONUNBUFFERED=1
RUN apk add --update --no-cache python3 py3-pip

COPY entrypoint.sh /
ENTRYPOINT ["sh", "/entrypoint.sh"]
EXPOSE 8080/tcp