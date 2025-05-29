package com.example.esee_poc.aop.interceptors;

import com.example.esee_poc.Constants;
import com.example.esee_poc.dto.CryptoConfigDto;
import com.example.esee_poc.dto.HeadersDataDto;
import com.example.esee_poc.entity.CryptoConfig;
import com.example.esee_poc.interfaces.Algorithm;
import com.example.esee_poc.repo.CryptoConfigRepo;
import com.example.esee_poc.utils.AlgorithmSelector;
import com.example.esee_poc.utils.Converter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.http.ResponseEntity;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
public class Interceptor {
    @Autowired
    CryptoConfigRepo cryptoConfigRepo;

    @Autowired
    AlgorithmSelector algorithmSelector;

    private HeadersDataDto fetchDataFromHeaders(HttpServletRequest request) {
        String clientId = request.getHeader(Constants.CLIENT_ID_HEADER_KEY);
        String partnerId = request.getHeader(Constants.PARTNER_ID_HEADER_KEY);
        String encryptionKey = request.getHeader(Constants.ENCRYPTION_KEY_HEADER_KEYS);
        String encryptedData = request.getHeader(Constants.ENCRYPTED_DATA_HEADER_KEY);
        String clientKeyVersion = request.getHeader(Constants.CLIENT_KEY_VERSION);

        List<String> encryptionKeys = List.of(encryptionKey.split("::"));

        return HeadersDataDto.builder()
                .clientId(clientId)
                .partnerId(partnerId)
                .encryptedKeys(encryptionKeys)
                .encryptedPayload(encryptedData)
                .clientKeyVersion(clientKeyVersion)
                .build();
    }

    private CryptoConfigDto fetchCryptoConfig(HeadersDataDto headersData) {
        CryptoConfig cryptoConfigData = cryptoConfigRepo.findFirstByPartnerIdAndClientIdAndIsDeletedFalseOrderByCreatedAtDesc(headersData.getPartnerId(), headersData.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Crypto configuration not found for partnerId: " + headersData.getPartnerId() + " and clientId: " + headersData.getClientId()));
        return Converter.toCryptoConfigDto(cryptoConfigData);
    }

    private String encryptPayload(CryptoConfigDto cryptoConfigData, Object payload, HeadersDataDto headersData) throws Exception {
        Algorithm<?> partnerAlgorithm = algorithmSelector.getAlgorithm(cryptoConfigData.getPartnerAlgorithm().getAlgorithm());

        if (partnerAlgorithm == null) {
            throw new IllegalArgumentException("Invalid algorithm configuration for partner or client.");
        }

        List<String> partnerKeys = partnerAlgorithm.fetchKey(cryptoConfigData, headersData.getClientKeyVersion());

        ObjectMapper objectMapper = new ObjectMapper();
        String payloadString = objectMapper.writeValueAsString(payload);

        return partnerAlgorithm.encrypt(payloadString, partnerKeys);
    }

    private String decryptPayload(CryptoConfigDto cryptoConfigData, HeadersDataDto headersData) throws Exception {
        Algorithm<?> partnerAlgorithm = algorithmSelector.getAlgorithm(cryptoConfigData.getPartnerAlgorithm().getAlgorithm());
        Algorithm<?> clientAlgorithm = algorithmSelector.getAlgorithm(cryptoConfigData.getClientAlgorithm().getAlgorithm());

        if (partnerAlgorithm == null || clientAlgorithm == null) {
            throw new IllegalArgumentException("Invalid algorithm configuration for partner or client.");
        }

        List<String> clientSecretKeys = clientAlgorithm.fetchKey(cryptoConfigData, headersData.getClientKeyVersion());

        // Decrypt the key using Client Algorithm
        List<String> decryptedKeys = headersData.getEncryptedKeys()
                                        .stream()
                                        .map((encryptedKey) -> {
                                            try {
                                                return (String) clientAlgorithm.decrypt(encryptedKey, clientSecretKeys);
                                            } catch (GeneralSecurityException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }).toList();

        // Decrypt the payload using Partner Algorithm
        return partnerAlgorithm.decrypt(headersData.getEncryptedPayload(), decryptedKeys);
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object decryptRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (request.getHeader(Constants.ENCRYPTION_KEY_HEADER_KEYS) == null || request.getHeader(Constants.ENCRYPTION_KEY_HEADER_KEYS).isEmpty()) {
            return joinPoint.proceed();
        }

        HeadersDataDto headersData = fetchDataFromHeaders(request);
        System.out.println(headersData.toString());

        CryptoConfigDto cryptoConfigData = fetchCryptoConfig(headersData);
        System.out.println(cryptoConfigData);
        String decryptedPayload = decryptPayload(cryptoConfigData, headersData);

        ArrayList<Object> argsArray = new ArrayList<>(List.of(joinPoint.getArgs()));

        ObjectMapper objectMapper = new ObjectMapper();
        Object decrypted = objectMapper.readValue(decryptedPayload, argsArray.getFirst().getClass());
        argsArray.set(0, decrypted);

        Object responsePayload = joinPoint.proceed(argsArray.toArray());
        if (responsePayload instanceof ResponseEntity<?> responseEntity) {
            Object responseBody = responseEntity.getBody();

            String responseBodyString = "";
            if (responseBody instanceof String) {
                responseBodyString = (String)responseBody;
            } else {
                responseBodyString  = objectMapper.writeValueAsString(responseEntity.getBody());
            }

            System.out.println(responseEntity.getBody());
            String encryptedResponse = encryptPayload(cryptoConfigData, responseBodyString, headersData);
            return ResponseEntity.ok(encryptedResponse);
        }

        throw new Exception("Expected ResponseEntity but got: " + responsePayload.getClass().getName());
    }
}
