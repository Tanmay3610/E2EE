package com.example.bank_sys.services;

import com.example.esee_poc.dto.CryptoConfigDto;
import com.example.esee_poc.entity.CryptoConfig;
import com.example.esee_poc.enums.AlgorithmTypes;
import com.example.esee_poc.interfaces.Algorithm;
import com.example.esee_poc.interfaces.VaultInteractionUtility;
import com.example.esee_poc.repo.CryptoConfigRepo;
import com.example.esee_poc.utils.AlgorithmSelector;
import com.example.esee_poc.utils.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CryptoService {
    @Autowired
    CryptoConfigRepo cryptoConfigRepo;

    @Autowired
    AlgorithmSelector algorithmSelector;

    @Autowired
    VaultInteractionUtility vaultInteractionUtility;

    public CryptoConfigDto getCryptoConfig(String partnerId, String clientId) {
        Optional<CryptoConfig> cryptoConfig = cryptoConfigRepo.findFirstByPartnerIdAndClientIdAndIsDeletedFalseOrderByCreatedAtDesc(partnerId, clientId);
        if (cryptoConfig.isPresent()) {
            return Converter.toCryptoConfigDto(cryptoConfig.get());
        } else {
            throw new IllegalArgumentException("Crypto configuration not found for partnerId: " + partnerId + " and clientId: " + clientId);
        }
    }

    private String generateKeyVersion(AlgorithmTypes algorithmName, int version) throws Exception {
        if (algorithmName.getAlgorithm().contains("-")) {
            throw new Exception("Algorithm name should not contain '-' character.");
        }
        return algorithmName + "-" + version;
    }

    private int extractKeyVersion(String version) throws Exception {
        if (!version.contains("-")) {
            throw new Exception("Version string must contain '-' character to separate algorithm name and version number.");
        }

        List<String> versionParts = List.of(version.split("-"));
        return Integer.parseInt(versionParts.get(1));
    }

    public String onboardClient(CryptoConfigDto config) {
        Algorithm<?> algorithm = algorithmSelector.getAlgorithm(config.getClientAlgorithm().getAlgorithm());
        try {
            CryptoConfig newConfig = Converter.toCryptoConfig(config);
            int currentKeyVersion = 1;
            newConfig.setCurrentKeyVersion(generateKeyVersion(config.getClientAlgorithm(), currentKeyVersion));
            newConfig.setCreatedAt(Instant.now());

            Object keysObj = algorithm.generateKey();
            algorithm.saveKeys(Converter.toCryptoConfigDto(newConfig), keysObj);

            cryptoConfigRepo.save(newConfig);
            return "Added Successfully";
        } catch (Exception e) {
            throw new RuntimeException("Key generation failed: " + e.getMessage(), e);
        }
    }

    public String rotateKeys(String clientId, String partnerId) throws Exception {
        Optional<CryptoConfig> configRes = cryptoConfigRepo.findFirstByPartnerIdAndClientIdAndIsDeletedFalseOrderByCreatedAtDesc(partnerId, clientId);
        if (configRes.isEmpty()) {
            return "Configuration not found for partnerId: " + partnerId + " and clientId: " + clientId;
        }

        CryptoConfig config = configRes.get();
        int currentKeyVersion = this.extractKeyVersion(config.getCurrentKeyVersion());
        String newKeyVersion = this.generateKeyVersion(config.getClientAlgorithm(), ++currentKeyVersion);

        Algorithm<?> algorithm = algorithmSelector.getAlgorithm(config.getClientAlgorithm().getAlgorithm());
        Object keys = algorithm.generateKey();

        CryptoConfig newConfig = CryptoConfig.builder()
                .currentKeyVersion(newKeyVersion)
                .partnerAlgorithm(config.getPartnerAlgorithm())
                .clientAlgorithm(config.getClientAlgorithm())
                .partnerId(config.getPartnerId())
                .clientId(config.getClientId())
                .createdAt(Instant.now())
                .build();

        algorithm.saveKeys(Converter.toCryptoConfigDto(newConfig), keys);
        cryptoConfigRepo.save(newConfig);

        return "Successfully rotated keys for clientId: " + clientId + " and partnerId: " + partnerId;
    }

    public String deleteKey(String clientId, String partnerId, String keyVersion) throws Exception {
        Optional<CryptoConfig> configRes = cryptoConfigRepo.findFirstByPartnerIdAndClientIdAndIsDeletedFalseOrderByCreatedAtDesc(partnerId, clientId);
        if (configRes.isEmpty()) {
            return "Configuration not found for partnerId: " + partnerId + " and clientId: " + clientId;
        }

        CryptoConfig config = configRes.get();
        Algorithm<?> algorithm = algorithmSelector.getAlgorithm(config.getClientAlgorithm().getAlgorithm());

        if (algorithm == null) {
            throw new IllegalArgumentException("Invalid algorithm specified in the request");
        }

        cryptoConfigRepo.deleteByClientIdAndPartnerIdAndCurrentKeyVersionAndIsDeletedFalse(clientId, partnerId, keyVersion);
        algorithm.removeKeys(Converter.toCryptoConfigDto(config), keyVersion);
        return "Successfully deleted key for clientId: " + clientId + " and partnerId: " + partnerId;
    }
}
