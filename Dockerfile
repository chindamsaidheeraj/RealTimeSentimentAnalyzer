FROM openjdk:11.0.13-jre-slim

COPY target/streamer-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
COPY test-2-3-mini.csv test-2-3-mini.csv
ENV ARGS ""
CMD java -jar app.jar $ARGS
