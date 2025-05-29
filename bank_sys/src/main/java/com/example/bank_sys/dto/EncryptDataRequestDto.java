package com.example.bank_sys.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncryptDataRequestDto {
    String currentKeyVersion;
    String data;
}
