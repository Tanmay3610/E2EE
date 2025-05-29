package com.example.bank_sys.controller;

import com.example.bank_sys.dto.AddBalanceRequestDto;
import com.example.bank_sys.services.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank/api")
public class BankController {
    @Autowired
    BankService bankService;

    @PostMapping("/balance")
    public ResponseEntity<AddBalanceRequestDto> getBalance(@RequestBody AddBalanceRequestDto addBalanceRequest) {
        return ResponseEntity.ok(bankService.addBalance(addBalanceRequest));
    }
}
