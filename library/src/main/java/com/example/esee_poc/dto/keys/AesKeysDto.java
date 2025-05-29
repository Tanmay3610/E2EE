package com.example.esee_poc.dto.keys;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AesKeysDto {
    byte[] masterKey;
    byte[] iv;
}
