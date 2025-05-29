package com.example.bank_sys.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddBalanceRequestDto {
    private String accountId;
    private String amount;

    @Override
    public String toString() {
        return "{" +
                "accountId='" + accountId + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }
}
