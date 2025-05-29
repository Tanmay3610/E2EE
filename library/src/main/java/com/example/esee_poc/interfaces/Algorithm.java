package com.example.esee_poc.interfaces;

import com.example.esee_poc.dto.CryptoConfigDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

@Component
public abstract class Algorithm<T> {
    @Autowired
    VaultInteractionUtility vaultInteractionUtility;

    protected List<String> fetchSecretKey(CryptoConfigDto config, List<String> keyTypes, String keyVersion) {
        Stream<String> keyNamesStream = keyTypes.stream().map((keyType) -> vaultInteractionUtility.getKeyName(config, keyType, keyVersion));

        return keyNamesStream.map((keyName) -> {
            try {
                return vaultInteractionUtility.getSecret(keyName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).map(secret -> {
            Base64.getEncoder().encodeToString(secret);
            return Base64.getEncoder().encodeToString(secret);
        }).toList();
    }

    protected void removeSecretKey(List<String> keyNames) {
        keyNames.forEach((keyName) -> {
            try {
                vaultInteractionUtility.removeSecret(keyName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Keys are provided to the algorithm, no need to interact with the Vault
    public abstract String encrypt(String data, List<String> keys) throws GeneralSecurityException, UnsupportedEncodingException;
    public abstract String decrypt(String data, List<String> keys) throws GeneralSecurityException;
    public abstract List<String> fetchKey(CryptoConfigDto config, String keyVersion) throws Exception;
    public abstract T generateKey() throws NoSuchAlgorithmException;
    public abstract void saveKeys(CryptoConfigDto config, Object keys) throws IOException;
    public abstract void removeKeys(CryptoConfigDto config, String keyVersion) throws IOException;
}
