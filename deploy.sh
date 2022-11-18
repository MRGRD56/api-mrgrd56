#!/bin/bash

git pull origin master
sudo docker-compose build
sudo docker-compose up -d