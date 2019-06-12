#!/bin/bash
 
if [[ ! $(cat /proc/1/sched | head -n 1 | grep init) ]]
then
   echo 'running in a docker container :-)'
else
   echo 'not running in a docker container - exit 1 to avoid destruction and crash :-)'
   exit 1
fi
VERSION="$1"
case "$VERSION" in
  '') echo 'version is the first and only parameter. Version is missing. Exit 1'
      exit 1 ;;
  *)  echo "creating an image for version $VERSION" ;;
esac

cd /opt
git clone --depth=1 -b develop https://github.com/rbudde/miniserver.git

cd /opt/miniserver
mvn clean install -DskipTests -DskipITs
docker build -t rbudde/miniserver:$VERSION -f Docker/Dockerfile .