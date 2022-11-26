FROM openjdk:17-jdk-slim

RUN mkdir /bot
RUN mkdir /data

COPY build/libs/Allium-*-all.jar /usr/local/lib/Allium.jar

# Only place env vars below that are fine to be publicised. Private stuff needs to be
# applied deployment-side.
# Optional: SENTRY_DSN

ENV TEST_SERVER=1004868734378319883
ENV TEST_CHANNEL=1013046925051834458

WORKDIR /bot

ENTRYPOINT ["java", "-Xms2G", "-Xmx2G", "-XX:+DisableExplicitGC", "-jar", "/usr/local/lib/Allium.jar"]