FROM eclipse-temurin:21-jdk AS builder

# Set working directory to the server folder
WORKDIR /app
COPY server /app

# Include gradle wrapper files needed to run the build
COPY gradlew gradlew
COPY gradle gradle

RUN chmod +x gradlew
RUN ./gradlew build -x test --no-daemon

# Runtime image
FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=builder /app /app

EXPOSE 4567
CMD ["./gradlew", "run", "--no-daemon"]
