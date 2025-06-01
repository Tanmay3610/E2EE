package com.example.esee_poc.aop.interceptors;

import com.example.esee_poc.Constants;
import com.example.esee_poc.dto.CryptoConfigDto;
import com.example.esee_poc.dto.HeadersDataDto;
import com.example.esee_poc.entity.CryptoConfig;
import com.example.esee_poc.interfaces.Algorithm;
import com.example.esee_poc.repo.CryptoConfigRepo;
import com.example.esee_poc.utils.AlgorithmSelector;
import com.example.esee_poc.utils.Converter;
import com.example.esee_poc.utils.CustomHttpInputMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.esee_poc.Constants.ENCRYPTED_BODY_KEY;

@ControllerAdvice
public class NewInterceptor extends RequestBodyAdviceAdapter {
    @Autowired
    CryptoConfigRepo cryptoConfigRepo;

    @Autowired
    AlgorithmSelector algorithmSelector;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader(Constants.ENCRYPTION_KEY_HEADER_KEYS) != null && !request.getHeader(Constants.ENCRYPTION_KEY_HEADER_KEYS).isEmpty();
    }

    private String streamToString(InputStream inputStream) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        try (InputStream input = inputStream) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while((bytesRead = input.read(buffer)) != -1) {
                textBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
        }

        return textBuilder.toString();
    }

    private HeadersDataDto fetchDataFromHeaders(HttpServletRequest request) {
        String clientId = request.getHeader(Constants.CLIENT_ID_HEADER_KEY);
        String partnerId = request.getHeader(Constants.PARTNER_ID_HEADER_KEY);
        String encryptionKey = request.getHeader(Constants.ENCRYPTION_KEY_HEADER_KEYS);
        String clientKeyVersion = request.getHeader(Constants.CLIENT_KEY_VERSION);

        List<String> encryptionKeys = List.of(encryptionKey.split("::"));

        return HeadersDataDto.builder()
                .clientId(clientId)
                .partnerId(partnerId)
                .encryptedKeys(encryptionKeys)
                .clientKeyVersion(clientKeyVersion)
                .build();
    }

    private CryptoConfigDto fetchCryptoConfig(HeadersDataDto headersData) {
        CryptoConfig cryptoConfigData = cryptoConfigRepo.findFirstByPartnerIdAndClientIdAndIsDeletedFalseOrderByCreatedAtDesc(headersData.getPartnerId(), headersData.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Crypto configuration not found for partnerId: " + headersData.getPartnerId() + " and clientId: " + headersData.getClientId()));
        return Converter.toCryptoConfigDto(cryptoConfigData);
    }

    private String decryptPayload(CryptoConfigDto cryptoConfigData, HeadersDataDto headersData, String encryptedPayload) throws Exception {
        System.out.println(encryptedPayload);
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

        System.out.println(decryptedKeys);
        // Decrypt the payload using Partner Algorithm
        return partnerAlgorithm.decrypt(encryptedPayload, decryptedKeys);
    }

    private String processValue(JsonNode value) {
        if (value.isTextual()) {
            String textValue = value.asText();
            if (textValue.startsWith("\"") && textValue.endsWith("\"")) {
                textValue = textValue.substring(1, textValue.length() - 1);
            }

            return textValue;
        }

        throw new IllegalArgumentException("Expected a text value but received: " + value.getNodeType());
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        String body = streamToString(inputMessage.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(body);

        AtomicReference<String> encryptedString = new AtomicReference<>("");
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;

            objectNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();

                if (Objects.equals(key, ENCRYPTED_BODY_KEY)) {
                    encryptedString.set(processValue(entry.getValue()));
                }
            });
        } else {
            throw new IllegalArgumentException("Expected JSON object but received: " + jsonNode.getNodeType());
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HeadersDataDto headersData = fetchDataFromHeaders(request);

        System.out.println(headersData.toString());
        CryptoConfigDto cryptoConfigData = fetchCryptoConfig(headersData);
        System.out.println(cryptoConfigData);

        String decryptedPayload = "";
        try {
            decryptedPayload = decryptPayload(cryptoConfigData, headersData, encryptedString.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println(decryptedPayload);
        return new CustomHttpInputMessage(new ByteArrayInputStream(decryptedPayload.getBytes(StandardCharsets.UTF_8)), inputMessage.getHeaders());
    }
}
