import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GatewayMetrics {
    private final Counter requestsCounter;

    public GatewayMetrics(MeterRegistry registry) {
        this.requestsCounter = Counter.builder("gateway.requests.total")
            .description("Total requests received by gateway")
            .register(registry);
    }

    public void incrementRequestCount() {
        requestsCounter.increment();
    }
}