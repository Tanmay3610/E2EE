package com.example.bank_sys.services;

import com.example.bank_sys.dto.AddBalanceRequestDto;
import org.springframework.stereotype.Service;

@Service
public class BankService {
    public AddBalanceRequestDto addBalance(AddBalanceRequestDto balanceAddRequest) {
        System.out.println(balanceAddRequest.toString());
        return balanceAddRequest;
    }
}
