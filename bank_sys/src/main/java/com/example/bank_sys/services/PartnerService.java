package com.example.bank_sys.services;

import com.example.bank_sys.dto.AddBalanceRequestDto;
import com.example.bank_sys.dto.EncryptDataRequestDto;
import com.example.bank_sys.dto.EncryptedKeysDto;
import com.example.esee_poc.dto.CryptoConfigDto;
import com.example.esee_poc.entity.CryptoConfig;
import com.example.esee_poc.interfaces.Algorithm;
import com.example.esee_poc.interfaces.VaultInteractionUtility;
import com.example.esee_poc.repo.CryptoConfigRepo;
import com.example.esee_poc.utils.AlgorithmSelector;
import com.example.esee_poc.utils.Converter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
public class PartnerService {
    @Autowired
    CryptoConfigRepo cryptoConfigRepo;

    @Autowired
    AlgorithmSelector algorithmSelector;

    public EncryptedKeysDto encryptKeys(String partnerId, String clientId, String keyVersion) throws Exception {
        Optional<CryptoConfig> optionalConfig = cryptoConfigRepo.findFirstByPartnerIdAndClientIdAndIsDeletedFalseOrderByCreatedAtDesc(partnerId, clientId);
        if (optionalConfig.isEmpty()) {
            throw new IllegalArgumentException("Crypto configuration not found for partnerId: " + partnerId + " and clientId: " + clientId);
        }

        CryptoConfig config = optionalConfig.get();

        Algorithm<?> partnerAlgorithm = algorithmSelector.getAlgorithm(config.getPartnerAlgorithm().getAlgorithm());
        Algorithm<?> clientAlgorithm = algorithmSelector.getAlgorithm(config.getClientAlgorithm().getAlgorithm());

        if (partnerAlgorithm == null || clientAlgorithm == null) {
            throw new IllegalArgumentException("Invalid algorithm specified in the request");
        }

        List<String> partnerKeys = partnerAlgorithm.fetchKey(Converter.toCryptoConfigDto(config), keyVersion);
        List<String> clientKeys = clientAlgorithm.fetchKey(Converter.toCryptoConfigDto(config), keyVersion);
        System.out.println(partnerKeys);
        System.out.println(clientKeys);
        List<String> encryptedKeys = partnerKeys.stream().map(
                (partnerKey) -> {
                    try {
                        return clientAlgorithm.encrypt(partnerKey, clientKeys);
                    } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).toList();

        return EncryptedKeysDto.builder().encryptedKeys(encryptedKeys).concatenatedKeys(String.join("::", encryptedKeys)).build();
    }

    public String startSession(String partnerId, String clientId) throws IOException, NoSuchAlgorithmException {
        Optional<CryptoConfig> optionalConfig = cryptoConfigRepo.findFirstByPartnerIdAndClientIdAndIsDeletedFalseOrderByCreatedAtDesc(partnerId, clientId);
        if (optionalConfig.isEmpty()) {
            return "Crypto configuration not found for partnerId: " + partnerId + " and clientId: " + clientId;
        }

        CryptoConfig config = optionalConfig.get();
        Algorithm<?> algorithm = algorithmSelector.getAlgorithm(config.getPartnerAlgorithm().getAlgorithm());

        CryptoConfigDto configDto = Converter.toCryptoConfigDto(config);
        algorithm.removeKeys(configDto, config.getCurrentKeyVersion());

        Object keys = algorithm.generateKey();
        algorithm.saveKeys(configDto, keys);
        return "Session started successfully";
    }

    public String encryptPayload(String partnerId, String clientId) throws Exception {
        Optional<CryptoConfig> optionalConfig = cryptoConfigRepo.findFirstByPartnerIdAndClientIdAndIsDeletedFalseOrderByCreatedAtDesc(partnerId, clientId);
        if (optionalConfig.isEmpty()) {
            return "Crypto configuration not found for partnerId: " + partnerId + " and clientId: " + clientId;
        }

        CryptoConfig config = optionalConfig.get();
        Algorithm<?> algorithm = algorithmSelector.getAlgorithm(config.getPartnerAlgorithm().getAlgorithm());

        List<String> keysObj = algorithm.fetchKey(Converter.toCryptoConfigDto(config), config.getCurrentKeyVersion());

        ObjectMapper objectMapper = new ObjectMapper();
        AddBalanceRequestDto balance = AddBalanceRequestDto.builder().accountId("123").amount("123").build();
        String balanceString = objectMapper.writeValueAsString(balance);

        keysObj.forEach(System.out::println);
        return algorithm.encrypt(balanceString, keysObj);
    }

    public String decryptData(String partnerId, String clientId, EncryptDataRequestDto request) throws Exception {
        Optional<CryptoConfig> optionalConfig = cryptoConfigRepo.findFirstByPartnerIdAndClientIdAndIsDeletedFalseOrderByCreatedAtDesc(partnerId, clientId);
        if (optionalConfig.isEmpty()) {
            throw new IllegalArgumentException("Crypto configuration not found for partnerId: " + partnerId + " and clientId: " + clientId);
        }

        CryptoConfig config = optionalConfig.get();
        Algorithm<?> partnerAlgorithm = algorithmSelector.getAlgorithm(config.getPartnerAlgorithm().getAlgorithm());

        List<String> keys = partnerAlgorithm.fetchKey(Converter.toCryptoConfigDto(config), request.getCurrentKeyVersion());
        return partnerAlgorithm.decrypt(request.getData(), keys);
    }
}
