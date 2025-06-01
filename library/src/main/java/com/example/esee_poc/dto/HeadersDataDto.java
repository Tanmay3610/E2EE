package com.example.esee_poc.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HeadersDataDto {
    @NotNull(message = "Client ID cannot be null")
    String clientId;

    @NotNull(message = "Partner ID cannot be null")
    String partnerId;

    @NotNull(message = "Client Key Version cannot be null")
    String clientKeyVersion;

    @NotNull(message = "Encrypted Keys cannot be null")
    List<String> encryptedKeys;

    @Override
    public String toString() {
        return "HeadersDataDto{" +
                "clientId='" + clientId + '\'' +
                ", partnerId='" + partnerId + '\'' +
                ", clientKeyVersion='" + clientKeyVersion + '\'' +
                ", encryptedKeys=" + encryptedKeys +
                '}';
    }
}
