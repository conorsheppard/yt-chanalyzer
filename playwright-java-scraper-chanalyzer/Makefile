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
	docker run --rm -p 5050:5050 conorsheppard/playwright-java-scraper-chanalyzer

debug:
	docker run --rm -p 5005:5005 \
      -e JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
      conorsheppard/playwright-java-scraper-chanalyzer

mvn-run:
	mvn spring-boot:run

test-coverage:
	mvn clean org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.12:report

check-coverage:
	open -a Google\ Chrome target/jacoco-report/index.html

coverage-badge-gen:
	python3 -m jacoco_badge_generator -j target/jacoco-report/jacoco.csv

test-suite: test-coverage check-coverage coverage-badge-gen

.SILENT:
.PHONY: default wrapper compile test mvn-build build run mvn-run test-coverage check-coverage coverage-badge-gen test-suite
