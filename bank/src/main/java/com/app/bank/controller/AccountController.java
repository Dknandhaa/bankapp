package com.app.bank.controller;

import com.app.bank.entity.Account;
import com.app.bank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    private Mono<Boolean> isAuthorized(String token) {
        // Extract the token value starting from the 7th character and remove unnecessary characters
        String realToken = token.substring(8,token.length()-1).replace("{token= ", "").replace("}", "");
        System.out.println("Extracted Token: " + realToken);

        // Prepare the validation request payload
        Map<String, String> validationRequest = Map.of("token", realToken);
        System.out.println("Validation Request Payload: " + validationRequest);

        // Call the validateToken method and check if the token is valid
        return accountService.validateToken(validationRequest)
                .doOnNext(response -> {
                    System.out.println("Received response from validateToken: " + response);
                })
                .map(response -> {
                    boolean isValid = "valid".equalsIgnoreCase(response.trim());
                    System.out.println("Token validation result: " + isValid);
                    return isValid;
                })
                .doOnNext(valid -> {
                    if (!valid) {
                        System.err.println("Unauthorized access: Invalid token");
                    }
                })
                .onErrorResume(ex -> {
                    System.err.println("Error validating token: " + ex.getMessage());
                    return Mono.just(false);
                });
    }

    @PostMapping
    public Mono<ResponseEntity<Account>> createAccount(@RequestBody Account account, @RequestHeader(value = "Authorization", required = true) String token) {
        return isAuthorized(token).flatMap(valid -> {
            if (!valid) {
                return Mono.just(ResponseEntity.status(401).build()); // Unauthorized
            }
            return Mono.just(ResponseEntity.ok(accountService.createAccount(account)));
        });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Account>> getAccount(@PathVariable Long id, @RequestHeader(value = "Authorization", required = true) String token) {
        return isAuthorized(token).flatMap(valid -> {
            if (!valid) {
                return Mono.just(ResponseEntity.status(401).build()); // Unauthorized
            }
            return Mono.justOrEmpty(accountService.getAccount(id))
                    .map(ResponseEntity::ok)
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        });
    }

    @PostMapping("/{id}/deposit")
    public Mono<ResponseEntity<Account>> deposit(@PathVariable Long id, @RequestBody Map<String, Double> request, @RequestHeader(value = "Authorization", required = true) String token) {
        return isAuthorized(token).flatMap(valid -> {
            if (!valid) {
                return Mono.just(ResponseEntity.status(401).build()); // Unauthorized
            }
            Double amount = request.get("amount");
            return Mono.just(ResponseEntity.ok(accountService.deposit(id, amount)));
        });
    }

    @PostMapping("/{id}/withdraw")
    public Mono<ResponseEntity<Account>> withdraw(@PathVariable Long id, @RequestBody Map<String, Double> request, @RequestHeader(value = "Authorization", required = true) String token) {
        return isAuthorized(token).flatMap(valid -> {
            if (!valid) {
                return Mono.just(ResponseEntity.status(401).build()); // Unauthorized
            }
            Double amount = request.get("amount");
            return Mono.just(ResponseEntity.ok(accountService.withdraw(id, amount)));
        });
    }

    @GetMapping("/roi")
    public Mono<ResponseEntity<Mono<Double>>> rate(@RequestHeader(value = "Authorization", required = true) String token) {
        return isAuthorized(token).flatMap(valid -> {
            if (!valid) {
                return Mono.just(ResponseEntity.status(401).build()); // Unauthorized
            }
            Mono<Double> interestRate = accountService.getInterestRate();
            return Mono.just(ResponseEntity.ok(interestRate));
        });
    }
}
