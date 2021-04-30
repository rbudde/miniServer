# MINI-SERVER

## Overview

Some systems are composed of a set of servers using http/https to communicate with each other and their environment ("distributed system" made out of
"micro services"):
* Each service is small and robust. 
* A service exposes features in a REST-like manner.
* Data exchange is based on JSON.
* Each service runs in a docker container of its own to allow separation of the services, easy scaling and simple(?) redeployment.

This repository provides a simple example to *understand* and *experiment* with this kind of architecture. It tries to avoid surprise and magic.
After working with this repository, the lots of magic behind frameworks as Spring Boot, DropWizard etc. should be easier to understand.
Then you may decide, to stay with lightweight frameworks as "miniserver" or switch to heavyweight frameworks.

There are lots of good tutorials in the web with similar goals. This is not the only one ... .

## Requirements

Everything is open-source and can be changed and used for private purposes without any restriction. The prerequisites to work with
this repository are the *installation* of
* Java >= 8.
* an IDE as Eclipse or Intellij.
* Git. It is great, that after installation you get a bash shell for free on Windows.
* Maven3.
* Docker.
  
## Check the installation

Use the Git bash on windows systems or almost any shell on linux systems. Check your PATH: you need access to both java and mvn.

1. create a directory for your git repositories, e.g. "git"
```sh
    mkdir git; cd git
```
   
2. get the git repository. It contains the branches master and develop. Checkout the develop branch
```sh
    git clone https://github.com/rbudde/miniserver.git
    cd miniserver
    git checkout develop
```
   
3. build the server (the first call caches a lot of data and needs internet to access repositories; later calls are faster and succeed off-line) 
```sh
    mvn clean install
```
   
4. start the server (below: the linux version. For win system, replace the ":" in the classpath parameter by ";" :-). Note the subtlety, that "*"
   is *not* expanded by the bash and double quotes are mandatory for win systems, but not foe linux.
   The VERSION variable must match the version value in the pom. Don't forget to stop the server later.
```sh
    # LINUX
    export VERSION=3.0.3-SNAPSHOT
    java -cp "target/MiniServer-${VERSION}.jar:target/lib/*" de.budde.jetty.ServerStarter
    # WINDOWS
    set VERSION=3.0.3-SNAPSHOT
    java -cp target/MiniServer-%VERSION%.jar;target/lib/* de.budde.jetty.ServerStarter
```
   
5. point your browser to *http://localhost:1998/simple/rest/hello* to issue a GET request. You should see a JSON object similar to this:
```sh
    { "greeting": "Hello!",
      "rnd": 30,
      "from": "Pid, the cavy",
      "to": "the world"
    }
```

6. the server can be put into a light-weight container, e.g. docker. Build the image, run the container.
   Check whether the server is running as expected (if that fails, probably port 1998 is in use, see 4.).
```sh
    docker build -f Docker/Dockerfile -t rbudde/miniserver .
    docker run --name miniserver0 -p 1998:1998 -d rbudde/miniserver
```

## Read the notes about software engineering

Toplevel in this repository there is a PDF document describing the rationale behind "miniserver". Enjoy!

For suggestions, discussions, questions contact me at reinhard.budde at iais.fraunhofer.de
