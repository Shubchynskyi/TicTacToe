#!/usr/bin/env bash
set -e

JAR_NAME="tictactoe-app-1.0.jar"
BUILDER_IMAGE="tictactoe-app-builder"
TEST_CONTAINER="tictactoe-app-test-container"

cleanup() {
  echo "Cleaning up..."
  docker rm "$TEST_CONTAINER" || true
  docker rmi "$BUILDER_IMAGE" || true
}

trap cleanup EXIT

echo "Building test image..."
docker build -t "$BUILDER_IMAGE" -f Docker-Build.Dockerfile .

echo "Running container for tests..."
docker run --name "$TEST_CONTAINER" "$BUILDER_IMAGE"

EXIT_CODE=$(docker inspect "$TEST_CONTAINER" --format='{{.State.ExitCode}}')
echo "Container exit code: $EXIT_CODE"

if [ "$EXIT_CODE" -ne 0 ]; then
  echo "Tests failed."
  echo "Container logs:"
  docker logs "$TEST_CONTAINER"
  exit 1
fi

echo "Copying JAR from container..."
docker cp "$TEST_CONTAINER":/app/target/"$JAR_NAME" "$JAR_NAME"

echo "Tests succeeded, JAR saved to $JAR_NAME"
