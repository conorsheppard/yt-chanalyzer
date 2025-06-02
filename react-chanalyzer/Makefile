SHELL := /bin/bash

default: build

build:
	docker build -f docker-compose/Dockerfile.local --no-cache . -t conorsheppard/react-chanalyzer

.SILENT:
.PHONY: default build
