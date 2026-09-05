FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM python:3.11-slim

ENV PYTHONUNBUFFERED=1
ENV FFMPEG_PATH=/usr/bin/ffmpeg
ENV EDGE_TTS_COMMAND=edge-tts
ENV WHISPER_COMMAND=whisper

RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-17-jre-headless ffmpeg libass9 \
    && pip install --no-cache-dir edge-tts openai-whisper \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/scraper-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
