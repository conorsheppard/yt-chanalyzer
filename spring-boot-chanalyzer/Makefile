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

db-start:
	docker run --name postgres-videos-db -e POSTGRES_USER=user -e POSTGRES_PASSWORD=root -e POSTGRES_DB=videosdb -p 5432:5432 -d postgres

db-reset:
	docker exec -it postgres-videos-db psql -U user -d videosdb -c "delete from scraped_videos; ALTER SEQUENCE scraped_videos_id_seq RESTART WITH 1;"

ssh-db:
	docker exec -it postgres-videos-db psql -U user -d videosdb

test-coverage:
	mvn clean org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.12:report

check-coverage:
	open -a Google\ Chrome target/jacoco-report/index.html

coverage-badge-gen:
	python3 -m jacoco_badge_generator -j target/jacoco-report/jacoco.csv

test-suite: test-coverage check-coverage coverage-badge-gen

.SILENT:
.PHONY: default install package test build db-start test-coverage check-coverage coverage-badge-gen test-suite
