# Stage 1: Build
FROM eclipse-temurin:17-jdk as builder
WORKDIR /app
COPY . .
RUN ./gradlew build --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app .
EXPOSE 4567
CMD ["./gradlew", "run", "--no-daemon"]


