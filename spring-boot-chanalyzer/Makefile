SHELL := /bin/bash

default: test

install:
	mvn clean install -U

package:
	mvn clean package

test:
	mvn test

test-coverage:
	mvn clean org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.12:report

check-coverage:
	open -a Google\ Chrome target/jacoco-report/index.html

.SILENT:
.PHONY: default install package test test-coverage check-coverage coverage-badge-gen