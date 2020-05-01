#!/bin/bash

if docker-compose ps -q; then
  docker-compose down -v
fi

docker-compose up -d
docker-compose logs -f
