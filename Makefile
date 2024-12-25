SHELL := /bin/bash

default: up

build:
	mvn clean package

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

dbu: down compose-build up

.PHONY: default build docker-nuke up-local up down down-local compose-build-local compose-build dbu