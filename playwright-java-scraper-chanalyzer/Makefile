SHELL := /bin/bash

default: run

wrapper:
	mvn wrapper:wrapper

compile:
	mvn compile

test:
	mvn test

mvn-build:
	mvn package -DskipTests

build: test
	docker build --no-cache . -t conorsheppard/playwright-java-scraper-chanalyzer

run:
	docker run -p 5050:5050 conorsheppard/playwright-java-scraper-chanalyzer

mvn-run:
	mvn spring-boot:run

.SILENT:
.PHONY: default wrapper compile test mvn-build build run mvn-run
