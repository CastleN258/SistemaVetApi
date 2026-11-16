FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN chmod +x gradlew
RUN ./gradlew installDist --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/install/SistemaVetApi /app

EXPOSE 10000

CMD ["/app/bin/SistemaVetApi"]
