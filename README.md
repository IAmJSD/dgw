# dgw
A pretty fast and stable gateway dispatcher written in Java. It dispatches to NATS. This was re-written in Rust and is distributed for multiple platforms.

It's worth noting all NATS events will be dispatched to `discord.*`. By default, if you have the guild members intent, it will send chunk payloads.

This Docker image is pullable from `ghcr.io/jakemakesstuff/dgw`.

## Environment variables
The following environment variables configure the application:
- `NATS_ADDR`: (Required) the address for your NATS host.
- `DISCORD_TOKEN`: (Required) your token to login to Discord.
- `SHARD_ID`/`SHARD_COUNT`: The Discord shard information. Defaults to ID 0/count 1.
- `INTENTS`: The integer representing the intents.
