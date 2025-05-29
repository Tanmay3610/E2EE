package com.example.esee_poc.interfaces;

import com.example.esee_poc.dto.CryptoConfigDto;

import java.io.IOException;

public interface VaultInteractionUtility {
    public String getKeyName(CryptoConfigDto config, String keyType, String keyVersion);
    public byte[] getSecret(String key) throws IOException;
    public void setSecret(String key, byte[] secret) throws IOException;
    public void removeSecret(String key) throws IOException;
}
