package com.example.esee_poc.dto.keys;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RSAKeysDto {
    byte[] publicKey;
    byte[] privateKey;
}
