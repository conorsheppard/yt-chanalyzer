SHELL := /bin/bash

default: up-local

build:
	./mvnw clean package -f spring-boot-chanalyzer/pom.xml

docker-nuke:
	docker ps -aq | tail -n+2 | grep . && docker stop $(docker ps -aq) || docker container prune -f && docker image prune -af && docker system prune -af && docker volume prune -f

up-local:
	docker-compose -f compose.yml up

up:
	docker-compose -f docker-compose.yml up

down-local:
	docker-compose -f compose.yml down

down:
	docker-compose -f docker-compose.yml down

compose-build-local:
	docker-compose -f compose.yml build --no-cache

compose-build:
	docker-compose -f docker-compose.yml build --no-cache

coverage-badge-gen:
	python3 -m jacoco_badge_generator -j spring-boot-chanalyzer/target/jacoco-report/jacoco.csv

dbu: down compose-build up

.SILENT:
.PHONY: default build docker-nuke up-local up down-local down compose-build-local compose-build dbu