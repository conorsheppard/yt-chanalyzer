SHELL := /bin/bash

default: run

run:
	npm start

build:
	docker build -f docker-compose/Dockerfile.local --no-cache . -t conorsheppard/react-chanalyzer

.SILENT:
.PHONY: default run build
