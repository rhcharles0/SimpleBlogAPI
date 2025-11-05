FROM gradle:9.2.0-jdk25 AS builder

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle --no-daemon clean build -x test
COPY /build/libs/*.jar ./app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination ./extracted

FROM bellsoft/liberica-openjre-alpine:25-cds
EXPOSE 8080
WORKDIR /app

COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

ENTRYPOINT ["java", "-jar", "./app.jar"]