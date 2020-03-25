#!/bin/bash

info () {
  echo -e "\e[32m$1"
}

error () {
  echo -e "\e[31m$1"
}

# Compile jMetal
mvn package

# Dockerize application
docker build --no-cache -f docker/Dockerfile-producer-demo -t producer-demo:v1 .
docker build --no-cache -f docker/Dockerfile-producer-demo-with-callbacks -t producer-demo-with-callbacks:v1 .
docker build --no-cache -f docker/Dockerfile-producer-demo-keys -t producer-demo-keys:v1 .
docker build --no-cache -f docker/Dockerfile-consumer-demo -t consumer-demo:v1 .
docker build --no-cache -f docker/Dockerfile-consumer-demo-groups -t consumer-demo-groups:v1 .
docker build --no-cache -f docker/Dockerfile-consumer-demo-with-threads -t consumer-demo-with-threads:v1 .
docker build --no-cache -f docker/Dockerfile-consumer-demo-assign-seek -t consumer-demo-assign-seek:v1 .
docker build --no-cache -f docker/Dockerfile-twitter-producer -t twitter-producer:v1 .