package com.s21.devops.sample.paymentsession.Controller;

import com.s21.devops.sample.paymentsession.Communication.PayReq;
import com.s21.devops.sample.paymentsession.Exception.NoPaymentException;
import com.s21.devops.sample.paymentsession.Exception.PaymentNotFoundException;
import com.s21.devops.sample.paymentsession.Service.PaymentService;
import com.s21.devops.sample.paymentsession.Service.SecurityService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final SecurityService securityService;
    private final Counter paymentRequestsCounter;

    @Autowired
    public PaymentController(PaymentService paymentService, 
                           SecurityService securityService,
                           MeterRegistry registry) {
        this.paymentService = paymentService;
        this.securityService = securityService;
        
        this.paymentRequestsCounter = Counter.builder("payment.requests.total")
                .description("Total payment requests")
                .register(registry);
    }

    @PostMapping("")
    public ResponseEntity<Void> pay(@Valid @RequestBody PayReq payReq)
            throws NoPaymentException {
        paymentRequestsCounter.increment();
        UUID paymentUid = paymentService.pay(payReq);
        if (paymentUid != null) {
            return ResponseEntity.created(ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{paymentUid}")
                    .buildAndExpand(paymentUid)
                    .toUri()
            ).build();
        } else {
            throw new NoPaymentException("Could not pay with such payment info!");
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{paymentUid}")
    public void refund(@PathVariable UUID paymentUid)
            throws PaymentNotFoundException {
        paymentRequestsCounter.increment();
        paymentService.delete(paymentUid);
    }

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(@RequestHeader("authorization") String authorization)
            throws InvalidKeySpecException, NoSuchAlgorithmException, EntityNotFoundException {
        paymentRequestsCounter.increment();
        String jwtToken = securityService.authorize(authorization);
        return ResponseEntity.ok().header("Authorization", "Bearer " + jwtToken).build();
    }
}