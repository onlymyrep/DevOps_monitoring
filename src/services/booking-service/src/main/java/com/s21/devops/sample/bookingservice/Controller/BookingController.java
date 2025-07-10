package com.s21.devops.sample.bookingservice.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.s21.devops.sample.bookingservice.Communication.*;
import com.s21.devops.sample.bookingservice.Exception.*;
import com.s21.devops.sample.bookingservice.Service.BookingService;
import com.s21.devops.sample.bookingservice.Service.SecurityService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

@EnableCircuitBreaker
@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {
    private final SecurityService securityService;
    private final BookingService bookingService;
    private final Counter bookingsCounter;
    private final Counter rabbitmqSentCounter;
    private final Counter rabbitmqProcessedCounter;

    @Autowired
    public BookingController(SecurityService securityService, 
                            BookingService bookingService,
                            MeterRegistry registry) {
        this.securityService = securityService;
        this.bookingService = bookingService;
        
        // Initialize metrics
        this.bookingsCounter = Counter.builder("bookings.total")
                .description("Total bookings created")
                .register(registry);
                
        this.rabbitmqSentCounter = Counter.builder("rabbitmq.sent.messages")
                .description("Messages sent to RabbitMQ")
                .register(registry);
                
        this.rabbitmqProcessedCounter = Counter.builder("rabbitmq.processed.messages")
                .description("Messages processed from RabbitMQ")
                .register(registry);
    }

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(@RequestHeader("authorization") String authorization)
            throws InvalidKeySpecException, NoSuchAlgorithmException, EntityNotFoundException {
        String jwtToken = securityService.authorize(authorization);
        return ResponseEntity.ok().header("Authorization", "Bearer " + jwtToken).build();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public void bookHotel(@Valid @RequestBody BookHotelReq bookHotelReq)
            throws ReservationAlreadyExistsException, NoPaymentException, CustomJwtException,
            CustomRuntimeException, PaymentNotFoundException, LoyaltyNotFoundException, JsonProcessingException, CoudntPayException {
        bookingsCounter.increment();
        rabbitmqSentCounter.increment();
        bookingService.bookHotel(bookHotelReq);
    }

    // This method should be called when processing RabbitMQ messages
    public void processRabbitMessage() {
        rabbitmqProcessedCounter.increment();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{hotelUid}")
    public void removeBooking(@PathVariable UUID hotelUid, @RequestParam UUID userUid)
            throws NoPaymentException, CustomJwtException, CustomRuntimeException, PaymentNotFoundException, JsonProcessingException {
        bookingService.removeBooking(hotelUid, userUid);
    }

    @GetMapping("/{hotelUid}")
    public BookingInfo getBookingInfo(@PathVariable UUID hotelUid, @RequestParam UUID userUid)
            throws ReservationNotFoundException, CustomJwtException, HotelNotFoundException, CustomRuntimeException {
        return bookingService.getBookingInfo(hotelUid, userUid);
    }

    @GetMapping("/{hotelUid}/rooms")
    public HotelsAavailabilityRes getHotelCapacity(@PathVariable UUID hotelUid, @RequestParam String from, @RequestParam String to)
            throws CustomJwtException, HotelNotFoundException, CustomRuntimeException {
        return bookingService.getHotelsAvailaibility(hotelUid, from, to);
    }

    @PostMapping("/{hotelUid}/rooms")
    public void patchFillingInfo(@PathVariable UUID hotelUid, @RequestBody PatchRoomsInfoReq patchRoomsInfoReq)
            throws JsonProcessingException {
        bookingService.patchHotelInfo(hotelUid, patchRoomsInfoReq);
    }

    @GetMapping("")
    public Iterable<BookingInfo> getAllBookingInfo(@RequestParam UUID userUid)
            throws ReservationNotFoundException, CustomJwtException, HotelNotFoundException, CustomRuntimeException {
        return bookingService.getAllBookingInfo(userUid);
    }
}