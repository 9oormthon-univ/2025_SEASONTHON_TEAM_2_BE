# 1단계: 빌드
FROM openjdk:17-jdk-slim AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# 2단계: 런타임
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# SSL 키스토어 마운트 타깃 디렉터리 생성
RUN mkdir -p /etc/certs

# 타임존/UTF-8 세팅
ENV TZ=Asia/Seoul
ENV LANG=C.UTF-8

COPY --from=builder /app/build/libs/*.jar app.jar

# HTTPS 포트 노출
EXPOSE 8443

ENTRYPOINT ["java", "-jar", "app.jar"]
