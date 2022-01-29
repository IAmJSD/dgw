#!/bin/sh
set -e
rm -rf build
./gradlew --no-daemon shadowJar
mv build/libs/dgw-*-all.jar dgw.jar
docker run --rm --privileged multiarch/qemu-user-static --reset -p yes
docker buildx create --use
docker buildx build --push --platform linux/arm64/v8,linux/amd64 --tag ghcr.io/jakemakesstuff/dgw -f Dockerfile.jar-runner .
rm dgw.jar
