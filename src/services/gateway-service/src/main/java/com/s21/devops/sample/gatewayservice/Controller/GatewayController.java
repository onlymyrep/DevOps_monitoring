package com.s21.devops.sample.gatewayservice.Controller;

import com.s21.devops.sample.gatewayservice.Communication.*;
import com.s21.devops.sample.gatewayservice.Exception.*;
import com.s21.devops.sample.gatewayservice.Service.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayController {
    private final SessionService sessionService;
    private final HotelsService hotelsService;
    private final BookingService bookingService;
    private final LoyaltyService loyaltyService;
    private final ReportService reportService;
    private final Counter gatewayRequestsCounter;
    private final GatewayMetrics metrics;

    @Autowired
    public GatewayController(SessionService sessionService,
                            HotelsService hotelsService,
                            BookingService bookingService,
                            LoyaltyService loyaltyService,
                            ReportService reportService,
                            MeterRegistry registry, GatewayMetrics metrics) {
        this.sessionService = sessionService;
        this.hotelsService = hotelsService;
        this.bookingService = bookingService;
        this.loyaltyService = loyaltyService;
        this.reportService = reportService;
        this.metrics = metrics;
        
        this.gatewayRequestsCounter = Counter.builder("gateway.requests.total")
                .description("Total gateway requests")
                .register(registry);
    }

    @PostMapping("/users")
    public void createUser(@Valid @RequestBody CreateUserReq createUserReq, @RequestHeader("Authorization") String authorization)
            throws UserAlreadyExistsException {
        gatewayRequestsCounter.increment();
        sessionService.createUser(createUserReq, authorization);
    }

        @PostMapping("/api/v1/bookings")
    public ResponseEntity<?> createBooking(...) {
        metrics.incrementRequestCount();
        
    }

    @GetMapping("/hotels")
    public HotelInfoRes[] getHotels()
            throws CustomJwtException, CustomRuntimeException {
        gatewayRequestsCounter.increment();
        return hotelsService.getAllHotels();
    }

    @GetMapping("/hotels/{hotelUid}")
    public HotelInfoRes getHotelInfo(@PathVariable UUID hotelUid)
            throws CustomJwtException, HotelNotFoundException, CustomRuntimeException {
        gatewayRequestsCounter.increment();
        return hotelsService.getHotel(hotelUid);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/booking")
    public void bookHotel(@Valid @RequestBody BookHotelReq bookHotelReq, @RequestHeader("Authorization") String authorization)
            throws CustomJwtException, ReservationAlreadyExistsException, CustomRuntimeException, HotelAlreadyExistsException {
        gatewayRequestsCounter.increment();
        bookHotelReq.setUserUid(getUserUid(authorization));
        bookingService.bookHotel(bookHotelReq);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/booking/{hotelUid}")
    public void removeBooking(@PathVariable UUID hotelUid, @RequestHeader("Authorization") String authorization)
            throws CustomJwtException, CustomRuntimeException, ReservationNotFoundException {
        gatewayRequestsCounter.increment();
        bookingService.removeBooking(hotelUid, getUserUid(authorization));
    }

    @GetMapping("/booking/{hotelUid}")
    public BookingInfo getBookingInfo(@PathVariable UUID hotelUid, @RequestHeader("Authorization") String authorization)
            throws CustomJwtException, CustomRuntimeException, ReservationNotFoundException {
        gatewayRequestsCounter.increment();
        return bookingService.getBookingInfo(hotelUid, getUserUid(authorization));
    }

    @GetMapping("/booking")
    public BookingInfo[] getAllBookingInfo(@RequestHeader("Authorization") String authorization)
            throws CustomJwtException, CustomRuntimeException, ReservationNotFoundException {
        gatewayRequestsCounter.increment();
        return bookingService.getAllBookingInfo(getUserUid(authorization));
    }

    @GetMapping("/booking/{hotelUid}/rooms")
    public HotelsAavailabilityRes getBookingAvailability(@PathVariable UUID hotelUid, @RequestParam String from, @RequestParam String to)
            throws CustomJwtException, CustomRuntimeException, HotelNotFoundException {
        gatewayRequestsCounter.increment();
        return bookingService.getHotelsAvailaibility(hotelUid, from, to);
    }

    @GetMapping("/loyalty")
    public LoyaltyBalanceRes getLoyaltyBalance(@RequestHeader("Authorization") String authorization)
            throws CustomJwtException, CustomRuntimeException {
        gatewayRequestsCounter.increment();
        try {
            return loyaltyService.getLoyaltyBalance(getUserUid(authorization));
        } catch (LoyaltyNotFoundException ex){
            return LoyaltyBalanceRes.loyaltyBalanceResFromParams("NO", 0.0);
        }
    }

    @PostMapping("/hotels")
    public ResponseEntity<Void> addHotel(@Valid @RequestBody CreateHotelReq createHotelReq)
            throws CustomRuntimeException, CustomJwtException, HotelAlreadyExistsException {
        gatewayRequestsCounter.increment();
        UUID hotelUid = hotelsService.createHotel(createHotelReq);
        return ResponseEntity.created(ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{hotelUid}")
                .buildAndExpand(hotelUid)
                .toUri()
        ).build();
    }

    @PatchMapping("/hotels/{hotelUid}/rooms")
    public void patchRoomsInfo(@PathVariable UUID hotelUid, @Valid @RequestBody PatchRoomsInfoReq patchRoomsInfoReq)
            throws CustomJwtException, CustomRuntimeException {
        gatewayRequestsCounter.increment();
        bookingService.patchRoomInfo(hotelUid, patchRoomsInfoReq);
    }

    @GetMapping("/reports/booking")
    public BookingStatisticsMessage[] getBookingStats(@RequestParam("from") String from, @RequestParam("to") String to)
            throws CustomJwtException, CustomRuntimeException {
        gatewayRequestsCounter.increment();
        return reportService.getUserStatistics(from, to);
    }

    @GetMapping("/reports/hotels-filling")
    public HotelFillingStatistics[] getFillingStats(@RequestParam("from") String from, @RequestParam("to") String to)
            throws CustomJwtException, CustomRuntimeException {
        gatewayRequestsCounter.increment();
        return reportService.getHotelStatistics(from, to);
    }

    private UUID getUserUid(String authorization) throws CustomJwtException, CustomRuntimeException {
        if (hasText(authorization) && authorization.startsWith("Bearer ")) {
            authorization = authorization.substring(7);
        }
        UserUidRes userUidRes = sessionService.validateToken(authorization);
        return userUidRes.getUserUid();
    }
}