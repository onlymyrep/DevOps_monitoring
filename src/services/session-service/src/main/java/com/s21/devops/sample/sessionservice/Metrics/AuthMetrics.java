import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AuthMetrics {
    private final Counter authRequestsCounter;

    public AuthMetrics(MeterRegistry registry) {
        this.authRequestsCounter = Counter.builder("auth.requests.total")
            .description("Total authentication requests")
            .register(registry);
    }

    public void incrementAuthRequest() {
        authRequestsCounter.increment();
    }
}