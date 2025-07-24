FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build -x test --no-daemon

# Optional: Create a smaller runtime image
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app /app

EXPOSE 4567
CMD ["./gradlew", "run", "--no-daemon"]
