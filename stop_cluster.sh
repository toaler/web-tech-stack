docker ps | grep web-tech-stack: | awk '{print $1}' | xargs docker stop --time 1

