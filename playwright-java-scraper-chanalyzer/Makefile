SHELL := /bin/bash

default: run

LATEST_VIDEO=$(shell find videos/ -type f -name "*.webm" -exec stat -f "%m %N" {} + | sort -nr | head -1 | awk '{print $$2}')

clean:
	mvn clean

docker-clean:
	docker image prune -f && docker rmi $(docker images | grep playwright-java-scraper-chanalyzer | awk '{print $3}') -f

wrapper:
	mvn wrapper:wrapper

compile:
	mvn compile

test:
	mvn test

mvn-build:
	mvn package -DskipTests

build: test
	docker build -f Dockerfile.local --no-cache . -t conorsheppard/playwright-java-scraper-chanalyzer

run:
	docker run --rm --name playwright-java-scraper-chanalyzer -p 5050:5050 conorsheppard/playwright-java-scraper-chanalyzer

debug:
	docker run --rm --name playwright-java-scraper-chanalyzer -p 5050:5050 -p 5005:5005 \
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

video-details:
	ls -lh videos/

playwright-codegen:
	# make interactive-scrape site=wikipedia.org
	npx playwright codegen $(site) --target=java

interactive-scrape: playwright-codegen

latest-video:
	@echo $(LATEST_VIDEO)

open-video:
	if [ -n "$(LATEST_VIDEO)" ]; then \
		echo "Opening video: $(LATEST_VIDEO)"; \
		open -a "Google Chrome" "$(LATEST_VIDEO)"; \
	else \
		echo "No video found."; \
	fi

clear-videos:
	rm -rf videos && mkdir -p videos

clear-cookies:
	rm cookies.json

.SILENT:
.PHONY: default clean docker-clean wrapper compile test mvn-build build run debug mvn-run test-coverage check-coverage coverage-badge-gen test-suite
