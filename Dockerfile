FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build --no-daemon

# Optional: Create a smaller runtime image
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app /app

EXPOSE 4567
CMD ["./gradlew", "run", "--no-daemon"]
