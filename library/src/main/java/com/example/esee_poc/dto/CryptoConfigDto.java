package com.example.esee_poc.dto;

import com.example.esee_poc.enums.AlgorithmTypes;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CryptoConfigDto {
    AlgorithmTypes clientAlgorithm;
    AlgorithmTypes partnerAlgorithm;
    String clientId;
    String currentKeyVersion;
    String partnerId;
    Instant createdAt;
    boolean isDeleted;

    @Override
    public String toString() {
        return "CryptoConfigDto{" +
                "clientAlgorithm=" + clientAlgorithm +
                ", partnerAlgorithm=" + partnerAlgorithm +
                ", clientId='" + clientId + '\'' +
                ", currentKeyVersion='" + currentKeyVersion + '\'' +
                ", partnerId='" + partnerId + '\'' +
                ", createdAt=" + createdAt +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
