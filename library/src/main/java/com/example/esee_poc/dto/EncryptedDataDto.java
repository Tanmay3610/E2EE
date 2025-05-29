package com.example.esee_poc.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EncryptedDataDto {
    public String body;

    @Override
    public String toString() {
        return "EncryptedDataDto{" +
                "body='" + body + '\'' +
                '}';
    }
}
