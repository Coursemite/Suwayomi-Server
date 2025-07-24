# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy everything
COPY . .

# Make gradlew executable
RUN chmod +x gradlew

# Build without running tests
RUN ./gradlew build -x test --no-daemon --stacktrace

# ---- Runtime stage ----
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy built project from builder image
COPY --from=builder /app /app

EXPOSE 4567

# Start the application
CMD ["./gradlew", "run", "--no-daemon"]
