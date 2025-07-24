# ---- Build Stage ----
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy the Gradle wrapper and build scripts first to leverage Docker cache
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY settings.gradle .
# Optional: if you have other config files like `build.gradle.kts`, include them too

# Make Gradle wrapper executable
RUN chmod +x gradlew

# Run a dummy build to download dependencies
RUN ./gradlew dependencies || true

# Now copy the rest of the source files
COPY . .

# Build the project without running tests
RUN ./gradlew build -x test --no-daemon --stacktrace

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=builder /app /app

EXPOSE 4567

CMD ["./gradlew", "run", "--no-daemon"]
