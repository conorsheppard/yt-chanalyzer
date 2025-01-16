SHELL := /bin/bash

default: test

test:
	mvn test

test-coverage:
	mvn clean org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.12:report

.SILENT:
.PHONY: default test