#!/usr/bin/env bash
set -e

JAR_NAME="tictactoe-app-1.0.jar"
FINAL_IMAGE="tictactoe-app-final"
APP_CONTAINER="tictactoe-app"
NETWORK_NAME="tictactoe-app-net"

echo "Removing old container if it exists..."
docker rm -f "$APP_CONTAINER" >/dev/null 2>&1 || true

echo "Removing old final image if it exists..."
docker rmi "$FINAL_IMAGE" >/dev/null 2>&1 || true

if [ ! -f "$JAR_NAME" ]; then
    echo "JAR file $JAR_NAME not found. Please run the test script first."
    exit 1
fi

echo "Building final image..."
docker build \
  --build-arg JAR_NAME="$JAR_NAME" \
  -t "$FINAL_IMAGE" \
  -f Docker-Final.Dockerfile .

echo "Checking if network $NETWORK_NAME exists..."
docker network inspect "$NETWORK_NAME" >/dev/null 2>&1 || docker network create "$NETWORK_NAME"

echo "Starting application container and connecting to network $NETWORK_NAME..."
docker run -d \
  --name "$APP_CONTAINER" \
  --network "$NETWORK_NAME" \
  "$FINAL_IMAGE"

echo "Application is starting in the background."
rm -f "$JAR_NAME"
echo "Removed local JAR file: $JAR_NAME"