# syntax=docker/dockerfile:1
FROM python:3.12-alpine
WORKDIR /app
COPY . /app
RUN pip3 install -r requirements.txt
EXPOSE 5050
CMD ["python", "flask-scraper-service.py"]