FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN ./gradlew build --no-daemon

EXPOSE 4567

CMD ["./gradlew", "run", "--no-daemon"]
