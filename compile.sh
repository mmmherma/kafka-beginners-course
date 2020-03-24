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
docker build --no-cache -f docker/Dockerfile -t producer-demo:v1 .