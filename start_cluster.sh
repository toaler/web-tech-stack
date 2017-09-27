#!/bin/bash

tag=web-tech-stack:0.0.1
hosts=(wts-1 wts-2 wts-3)
containers=()

for h in ${hosts[@]}; do
  echo "setting up host=${h}"
  container_id=`docker run -d -h ${h} -it ${tag}`
  echo "started container id = ${container_id}"
done

docker ps

