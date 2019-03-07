DOCKER  := docker
REGISTRY:= repo.adeo.no:5443

.PHONY: all build upload

all: build upload

build:
	$(DOCKER) run --rm -t \
		-v ${PWD}:/usr/src \
		-w /usr/src \
		-u $(shell id -u) \
		-v ${HOME}/.m2:/var/maven/.m2 \
		-e MAVEN_CONFIG=/var/maven/.m2 \
		maven:3.5-jdk-11 mvn -Duser.home=/var/maven verify -B -V

upload:
	$(DOCKER) run --rm -t \
		-v ${PWD}:/usr/src \
		-w /usr/src \
		-u $(shell id -u) \
		-v ${HOME}/.m2:/var/maven/.m2 \
		-e MAVEN_CONFIG=/var/maven/.m2 \
		maven:3.5-jdk-11 mvn -Duser.home=/var/maven -B -V -fn deploy -DskipTests -Dmaven.install.skip=true
