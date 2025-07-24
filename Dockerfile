# ---- Build Stage ----
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper and related files
COPY gradlew .
COPY gradle gradle

# Copy build scripts
COPY build.gradle.kts .
COPY settings.gradle.kts .
# COPY settings.gradle .  <-- REMOVE THIS LINE

# Copy the rest of the project
COPY . .

# Make Gradle wrapper executable
RUN chmod +x gradlew

# Optional: force Gradle to resolve dependencies early
RUN ./gradlew dependencies || true

# Build the project without running tests
RUN ./gradlew build -x test --no-daemon --stacktrace

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=builder /app /app

EXPOSE 4567

CMD ["./gradlew", "run", "--no-daemon"]
