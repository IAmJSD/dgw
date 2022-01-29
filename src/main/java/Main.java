import io.nats.client.Connection;
import io.nats.client.Nats;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;

class Main {
    public static String envVarMust(String envVar) {
        var x = System.getenv(envVar);
        if (x == null) {
            System.err.println(envVar + " not specified.");
            System.exit(1);
        }
        return x;
    }

    public static int parseInt(String key, String value, int nullValue) {
        if (value == null) return nullValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println(key + " is not an integer.");
            System.exit(1);
        }
        return 0;
    }

    public static void main(String[] args) {
        // Get the NATS address.
        var natsAddr = envVarMust("NATS_ADDR");

        // Get the variables needed for discord.
        var token = envVarMust("DISCORD_TOKEN");
        var shardIdStr = System.getenv("SHARD_ID");
        var shardCountStr = System.getenv("SHARD_COUNT");
        var shardId = parseInt("SHARD_ID", shardIdStr, 0);
        var shardCount = parseInt("SHARD_COUNT", shardCountStr, 1);
        var intentsStr = envVarMust("INTENTS");
        var intents = parseInt("INTENTS", intentsStr, 0);

        // Make the connection to NATS.
        Connection nc;
        try {
            nc = Nats.connect(natsAddr);
        } catch (Exception e) {
            System.err.println("Failed to make NATS connection: " + e.getMessage());
            System.exit(1);
            return;
        }

        // Make the connection to Discord.
        try {
            JDABuilder
                .createLight(token)
                .setEnabledIntents(GatewayIntent.getIntents(intents))
                .setRawEventsEnabled(true)
                .addEventListeners(new NATSDispatcher(nc))
                .useSharding(shardId, shardCount)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build()
                .awaitReady();
        } catch (Exception e) {
            System.err.println("Failed to run bot: " + e.getMessage());
            System.exit(1);
        }
    }
}
