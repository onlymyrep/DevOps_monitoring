package com.s21.devops.sample.loyaltyservice.Controller;

import com.s21.devops.sample.loyaltyservice.Communication.ChargeBalanceReq;
import com.s21.devops.sample.loyaltyservice.Communication.LoyaltyBalanceRes;
import com.s21.devops.sample.loyaltyservice.Exception.LoyaltyNotFoundException;
import com.s21.devops.sample.loyaltyservice.Service.LoyaltyService;
import com.s21.devops.sample.loyaltyservice.Service.SecurityService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loyalty")
public class LoyaltyController {
    private final SecurityService securityService;
    private final LoyaltyService loyaltyService;
    private final Counter loyaltyRequestsCounter;

    @Autowired
    public LoyaltyController(SecurityService securityService, 
                           LoyaltyService loyaltyService,
                           MeterRegistry registry) {
        this.securityService = securityService;
        this.loyaltyService = loyaltyService;
        
        this.loyaltyRequestsCounter = Counter.builder("loyalty.requests.total")
                .description("Total loyalty requests")
                .register(registry);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{userUid}")
    public void charge(@PathVariable UUID userUid, @RequestBody ChargeBalanceReq chargeBalanceReq) {
        loyaltyRequestsCounter.increment();
        loyaltyService.chargeBalance(userUid, chargeBalanceReq);
    }

    @GetMapping("/{userUid}")
    public LoyaltyBalanceRes getBalance(@PathVariable UUID userUid)
            throws LoyaltyNotFoundException {
        loyaltyRequestsCounter.increment();
        return loyaltyService.getLoyaltyBalance(userUid);
    }

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(@RequestHeader("authorization") String authorization)
            throws InvalidKeySpecException, NoSuchAlgorithmException, EntityNotFoundException {
        loyaltyRequestsCounter.increment();
        String jwtToken = securityService.authorize(authorization);
        return ResponseEntity.ok().header("Authorization", "Bearer " + jwtToken).build();
    }
}