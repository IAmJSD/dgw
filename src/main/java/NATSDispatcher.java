import io.nats.client.Connection;
import net.dv8tion.jda.api.events.RawGatewayEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

class NATSDispatcher extends ListenerAdapter {
    private final Connection nc;

    NATSDispatcher(Connection nc) {
        this.nc = nc;
    }

    @Override
    public void onRawGateway(RawGatewayEvent event) {
        String type = event.getType().toLowerCase();
        nc.publish("discord." + type, event.getPayload().toJson());
    }
}
