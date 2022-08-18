use std::{env, str};
use twilight_model::{gateway::payload::outgoing::RequestGuildMembers};
use twilight_gateway::{Intents, EventTypeFlags, Event, Shard};
use futures_util::StreamExt;
use serde::Deserialize;
use serde_json::from_slice;
use bytes::Bytes;

fn string_var_must(name: &str) -> String {
    return env::var(name).unwrap();
}

fn int_var(name: &str, default: u64) -> u64 {
    return env::var(name).unwrap_or(default.to_string()).parse().unwrap();
}

#[derive(Deserialize)]
struct GatewayData<'a> {
    t: Option<&'a str>,
    d: Option<&'a serde_json::value::RawValue>,
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    // Initialize the tracing subscriber.
    tracing_subscriber::fmt::init();

    // Get all the variables to begin with.
    let nats_addr = string_var_must("NATS_ADDR");
    let discord_token = string_var_must("DISCORD_TOKEN");
    let shard_id = int_var("SHARD_ID", 0);
    let shard_count = int_var("SHARD_COUNT", 1);
    let intents = int_var("INTENTS", 0);

    // Connect to nats.
    let nats = async_nats::connect(nats_addr).await;
    if nats.is_err() {
        panic!("{}", nats.err().unwrap());
    }
    let client = nats.unwrap();

    let (shard, mut events) = Shard::builder(discord_token, Intents::from_bits_truncate(intents)).
        event_types(EventTypeFlags::SHARD_PAYLOAD | EventTypeFlags::GUILD_CREATE).
        shard(shard_id, shard_count).unwrap().
        build();
    shard.start().await?;

    while let Some(event) = events.next().await {
        match event {
            Event::GuildCreate(guild) => {
                // Let's request all of the guild's members for caching.
                shard
                    .command(&RequestGuildMembers::builder(guild.id).query("", None))
                    .await?;  
            }
            Event::ShardPayload(payload) => {
                // Send the payload to nats.
                let gw: GatewayData = from_slice(&payload.bytes).unwrap();
                if !gw.d.is_none() && !gw.t.is_none() {
                    let v = serde_json::to_vec(gw.d.unwrap()).unwrap();
                    client.publish(format!("discord.{}", gw.t.unwrap().to_ascii_lowercase()), Bytes::copy_from_slice(&v)).await?;
                }
            }
            _ => {}
        }
    }

    Ok(())
}
