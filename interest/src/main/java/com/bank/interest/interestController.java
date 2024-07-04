package com.bank.interest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class interestController {

    @Autowired
    private BankInterestService bankInterestService;

    @GetMapping("/rateOfInterest")
    public ResponseEntity<Double> roi() {
        double interestRate = bankInterestService.rateOfInterest();
        return ResponseEntity.ok(interestRate);
    }
}
