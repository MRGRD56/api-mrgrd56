#!/bin/bash

sudo -u user git pull origin master
sudo docker-compose build
sudo docker-compose up -d
