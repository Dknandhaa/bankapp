package com.app.bank.service;

import com.app.bank.entity.Account;
import com.app.bank.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${app2.base.url}")
    private String app2BaseUrl;

    @Value("${app3.base.url}")
    private String app3BaseUrl;

    public Mono<String> registerUser(Object user) {
        return webClientBuilder.build()
                .post()
                .uri(app2BaseUrl + "/register")
                .bodyValue(user)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> generateToken(Object tokenReqRes) {
        return webClientBuilder.build()
                .post()
                .uri(app2BaseUrl + "/generate-token")
                .bodyValue(tokenReqRes)
                .retrieve()
                .bodyToMono(String.class);
    }


    public Mono<String> validateToken(Object tokenReqRes) {
        return webClientBuilder.build()
                .post()
                .uri(app2BaseUrl + "/validate-token")
                .bodyValue(tokenReqRes)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<Double> getInterestRate() {
        return webClientBuilder.build()
                .get()
                .uri(app3BaseUrl + "/rateOfInterest")
                .retrieve()
                .bodyToMono(Double.class)
                .doOnError(error -> {
                    // Log the error
                    System.err.println("Error fetching interest rate: " + error.getMessage());
                })
                .onErrorResume(ex -> Mono.empty());
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Optional<Account> getAccount(Long id) {
        return accountRepository.findById(id);
    }

    public Account deposit(Long id, double amount) {
        Account account = getAccount(id).orElseThrow(() -> new RuntimeException("Account not found"));
        account.setBalance(account.getBalance() + amount);
        return accountRepository.save(account);
    }

    public Account withdraw(Long id, double amount) {
        Account account = getAccount(id).orElseThrow(() -> new RuntimeException("Account not found"));
        if (account.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds");
        }
        account.setBalance(account.getBalance() - amount);
        return accountRepository.save(account);
    }
}
