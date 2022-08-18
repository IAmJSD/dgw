FROM rust:1.63-alpine
RUN apk add cmake make gcc g++ openssl-dev
WORKDIR /app
COPY . .
RUN cargo build --release

FROM alpine:3.16.2
RUN apk add --no-cache ca-certificates
WORKDIR /app
COPY --from=0 /app/target/release/dgw /app/dgw
CMD ["/app/dgw"]
