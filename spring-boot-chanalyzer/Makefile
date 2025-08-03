SHELL := /bin/bash

default: test

install:
	mvn clean install -U

package:
	mvn clean package

test:
	mvn test

build:
	docker build -f docker-compose/Dockerfile.local --no-cache . -t conorsheppard/spring-boot-chanalyzer

run:
	docker run --name spring-boot-chanalyzer \
	--network yt-chanalyzer_default \
	-e SCRAPER_URL_LOCAL=http://playwright-java-scraper-chanalyzer:5050/api/v1/channels \
	-e SPRING_PROFILES_ACTIVE=default \
	-e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/videosdb \
	-e SPRING_DATASOURCE_USERNAME=user \
	-e SPRING_DATASOURCE_PASSWORD=root \
	-p 8080:8080 conorsheppard/spring-boot-chanalyzer:latest

debug:
	docker run --name spring-boot-chanalyzer \
	--network yt-chanalyzer_default \
	-e SCRAPER_URL_LOCAL=http://playwright-java-scraper-chanalyzer:5050/api/v1/channels \
	-e SPRING_PROFILES_ACTIVE=default \
	-e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/videosdb \
	-e SPRING_DATASOURCE_USERNAME=user \
	-e SPRING_DATASOURCE_PASSWORD=root \
	-p 8080:8080 -p 5005:5005 \
	-e JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
	conorsheppard/spring-boot-chanalyzer:latest

db-start:
	docker run --name postgres-chanalyzer -e POSTGRES_USER=user -e POSTGRES_PASSWORD=root -e POSTGRES_DB=videosdb -p 5432:5432 -d postgres

db-reset:
	docker exec -it postgres-chanalyzer psql -U user -d videosdb -c "delete from scrape_status; delete from scraped_videos; ALTER SEQUENCE scraped_videos_id_seq RESTART WITH 1;"

ssh-db:
	docker exec -it postgres-chanalyzer psql -U user -d videosdb

test-coverage:
	mvn clean org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.12:report

check-coverage:
	open -a Google\ Chrome target/jacoco-report/index.html

coverage-badge-gen:
	python3 -m jacoco_badge_generator -j target/jacoco-report/jacoco.csv

test-suite: test-coverage check-coverage coverage-badge-gen

.SILENT:
.PHONY: default install package test build db-start db-reset ssh-db test-coverage check-coverage coverage-badge-gen test-suite
