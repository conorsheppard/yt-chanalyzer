# syntax=docker/dockerfile:1
FROM python:3.12-alpine
WORKDIR /app
COPY . /app
RUN pip3 install -r requirements.txt
EXPOSE 5050
#ENTRYPOINT ["sh", "entrypoint.sh"]
CMD ["python", "flask-scraper-service.py"]

# CMD python ./flask-scraper-service.py

# CMD flask run --no-reload \
#   --with-threads \
#   --host 0.0.0.0 --port 5000

# CMD [ "flask", "--app", "flask-scraper-service", "run", "--host=0.0.0.0" ]