package com.s21.devops.sample.hotelservice.Controller;

import com.s21.devops.sample.hotelservice.Communication.CreateHotelReq;
import com.s21.devops.sample.hotelservice.Communication.HotelCapacityRes;
import com.s21.devops.sample.hotelservice.Communication.HotelInfoRes;
import com.s21.devops.sample.hotelservice.Exception.HotelAlreadyExistsException;
import com.s21.devops.sample.hotelservice.Exception.HotelNotFoundException;
import com.s21.devops.sample.hotelservice.Service.HotelService;
import com.s21.devops.sample.hotelservice.Service.SecurityService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hotels")
public class HotelsController {
    private final SecurityService securityService;
    private final HotelService hotelService;
    private final Counter hotelInfoRequestsCounter;

    @Autowired
    public HotelsController(SecurityService securityService, 
                           HotelService hotelService,
                           MeterRegistry registry) {
        this.securityService = securityService;
        this.hotelService = hotelService;
        
        this.hotelInfoRequestsCounter = Counter.builder("hotel.info.requests")
                .description("Hotel info requests")
                .register(registry);
    }

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(@RequestHeader("authorization") String authorization)
            throws InvalidKeySpecException, NoSuchAlgorithmException, EntityNotFoundException {
        String jwtToken = securityService.authorize(authorization);
        return ResponseEntity.ok().header("Authorization", "Bearer " + jwtToken).build();
    }

    @GetMapping("")
    public Iterable<HotelInfoRes> getHotels() {
        hotelInfoRequestsCounter.increment();
        return hotelService.getAllHotels();
    }

    @GetMapping("/{hotelUid}")
    public HotelInfoRes getHotelInfo(@PathVariable UUID hotelUid)
            throws HotelNotFoundException {
        hotelInfoRequestsCounter.increment();
        return hotelService.getHotel(hotelUid);
    }

    @GetMapping("/{hotelUid}/rooms")
    public HotelCapacityRes getHotelCapacity(@PathVariable UUID hotelUid)
            throws HotelNotFoundException {
        hotelInfoRequestsCounter.increment();
        return hotelService.getHotelCapacity(hotelUid);
    }

    @PostMapping("")
    public ResponseEntity<Void> addHotel(@Valid @RequestBody CreateHotelReq createHotelReq)
            throws HotelAlreadyExistsException {
        hotelInfoRequestsCounter.increment();
        UUID hotelUid = hotelService.createHotel(createHotelReq);
        return ResponseEntity.created(ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{hotelUid}")
                .buildAndExpand(hotelUid)
                .toUri()
        ).build();
    }
}