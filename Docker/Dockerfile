FROM openjdk:8u151-jdk

ARG LAST_RUN_OF_CREATE_MINISERVER=2017-12-11-12:40:00
 
RUN mkdir --parent /opt/MiniServer/lib /opt/MiniServer/staticResources

WORKDIR /opt/MiniServer

EXPOSE 1998
 
ADD ["staticResources","./staticResources/"]
ADD ["target/lib","./lib/"]
ADD ["target/MiniServer-*.jar","./lib"]
 
ENTRYPOINT ["java", "-Xmx1G", "-cp", "lib/*", "de.budde.jetty.ServerStarter"]