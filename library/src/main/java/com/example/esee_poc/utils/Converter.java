package com.example.esee_poc.utils;

import com.example.esee_poc.dto.CryptoConfigDto;
import com.example.esee_poc.entity.CryptoConfig;

public class Converter {
    public static CryptoConfigDto toCryptoConfigDto(CryptoConfig config) {
        return CryptoConfigDto.builder()
                .clientAlgorithm(config.getClientAlgorithm())
                .partnerAlgorithm(config.getPartnerAlgorithm())
                .clientId(config.getClientId())
                .currentKeyVersion(config.getCurrentKeyVersion())
                .partnerId(config.getPartnerId())
                .isDeleted(config.isDeleted())
                .createdAt(config.getCreatedAt())
                .build();
    }

    public static CryptoConfig toCryptoConfig(CryptoConfigDto config) {
        return CryptoConfig.builder()
                .clientAlgorithm(config.getClientAlgorithm())
                .partnerAlgorithm(config.getPartnerAlgorithm())
                .clientId(config.getClientId())
                .currentKeyVersion(config.getCurrentKeyVersion())
                .partnerId(config.getPartnerId())
                .isDeleted(config.isDeleted())
                .createdAt(config.getCreatedAt())
                .build();
    }
}
