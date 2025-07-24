# ---- Build Stage ----
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy full project
COPY . .

# Make Gradle wrapper executable
RUN chmod +x gradlew

# Build the project without running tests
RUN ./gradlew build -x test --no-daemon --stacktrace

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=builder /app /app

EXPOSE 4567

CMD ["./gradlew", "run", "--no-daemon"]

