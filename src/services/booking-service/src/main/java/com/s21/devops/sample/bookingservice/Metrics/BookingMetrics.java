import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BookingMetrics {
    private final Counter bookingsCounter;
    private final Counter messagesSentCounter;
    private final Counter messagesProcessedCounter;

    public BookingMetrics(MeterRegistry registry) {
        this.bookingsCounter = Counter.builder("bookings.total")
            .description("Total number of bookings")
            .register(registry);
        
        this.messagesSentCounter = Counter.builder("rabbitmq.messages.sent")
            .description("Messages sent to RabbitMQ")
            .register(registry);
            
        this.messagesProcessedCounter = Counter.builder("rabbitmq.messages.processed")
            .description("Messages processed from RabbitMQ")
            .register(registry);
    }

    public void incrementBookings() {
        bookingsCounter.increment();
    }

    public void incrementMessagesSent() {
        messagesSentCounter.increment();
    }

    public void incrementMessagesProcessed() {
        messagesProcessedCounter.increment();
    }
}