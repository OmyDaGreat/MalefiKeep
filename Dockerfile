# syntax=docker/dockerfile:1

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

ARG KOBWEB_CLI_VERSION=0.9.21

RUN apt-get update && apt-get install -y --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/*

# Download kobweb CLI
RUN curl -fsSL \
    "https://github.com/varabyte/kobweb-cli/releases/download/v${KOBWEB_CLI_VERSION}/kobweb-${KOBWEB_CLI_VERSION}.zip" \
    -o kobweb.zip \
    && unzip -q kobweb.zip \
    && rm kobweb.zip

# Copy build files first (layer cache for dependency downloads)
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q || true

# Copy source and export
COPY site/ site/
RUN cd site && ../kobweb-${KOBWEB_CLI_VERSION}/bin/kobweb export --notty

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

ARG KOBWEB_CLI_VERSION=0.9.21

COPY --from=builder /app/kobweb-${KOBWEB_CLI_VERSION}/ kobweb/
COPY --from=builder /app/site/.kobweb/ site/.kobweb/
COPY --from=builder /app/site/build/ site/build/

EXPOSE 8080

# Run kobweb server in production mode
CMD ["kobweb/bin/kobweb", "run", "--env", "prod", "--notty"]
