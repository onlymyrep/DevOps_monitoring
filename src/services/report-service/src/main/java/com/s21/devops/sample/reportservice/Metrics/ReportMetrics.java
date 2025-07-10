import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ReportMetrics {
    private final Counter messagesProcessedCounter;

    public ReportMetrics(MeterRegistry registry) {
        this.messagesProcessedCounter = Counter.builder("rabbitmq.messages.processed")
            .description("Messages processed from RabbitMQ")
            .register(registry);
    }

    public void incrementMessagesProcessed() {
        messagesProcessedCounter.increment();
    }
}