package com.example.esee_poc.utils.algorithms;

import com.example.esee_poc.Constants;
import com.example.esee_poc.dto.CryptoConfigDto;
import com.example.esee_poc.dto.keys.AesKeysDto;
import com.example.esee_poc.enums.KeyTypes;
import com.example.esee_poc.interfaces.Algorithm;
import com.example.esee_poc.interfaces.VaultInteractionUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component(Constants.AES_ALGORITHM_BEAN_NAME)
public class AES_256_CBC extends Algorithm<AesKeysDto> {
    AES_256_CBC() {
        super();
    }

    @Autowired
    VaultInteractionUtility vaultInteractionUtility;

    private List<byte[]> toBytesArray(List<String> keys) {
        return keys.stream()
                .map(Base64.getDecoder()::decode)
                .toList();
    }

    @Override
    public String encrypt(String data, List<String> keys) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        List<byte[]> keyArrayInBytes = this.toBytesArray(keys);
        byte[] masterKey = keyArrayInBytes.get(0);
        byte[] iv = keyArrayInBytes.get(1);

        System.out.println("IV" + Arrays.toString(iv));
        SecretKey secretKey = new SecretKeySpec(masterKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] ciphertextBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(ciphertextBytes);
    }

    @Override
    public String decrypt(String data, List<String> keys) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        List<byte[]> keyArrayInBytes = this.toBytesArray(keys);
        byte[] masterKey = keyArrayInBytes.get(0);
        byte[] iv = keyArrayInBytes.get(1);

        SecretKey secretKey = new SecretKeySpec(masterKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Initialize AES cipher in CBC mode with PKCS5 padding
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] ciphertextBytes = Base64.getDecoder().decode(data);

        byte[] plaintextBytes = cipher.doFinal(ciphertextBytes);
        return new String(plaintextBytes, StandardCharsets.UTF_8);
    }

    public List<String> fetchKey(CryptoConfigDto config, String keyVersion) throws Exception {
        List<String> keyTypes = new ArrayList<>();
        keyTypes.add(KeyTypes.AES_MASTER_KEY.getKeyName());
        keyTypes.add(KeyTypes.AES_IV_KEY.getKeyName());

        return this.fetchSecretKey(config, keyTypes, keyVersion);
    }

    @Override
    public AesKeysDto generateKey() throws NoSuchAlgorithmException {
        int keySize = 256;
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize);
        SecretKey key = keyGenerator.generateKey();
        byte[] masterKey = key.getEncoded();

        // Creating a random IV (Initialization Vector)
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);

        return AesKeysDto.builder().masterKey(masterKey).iv(iv).build();
    }

    @Override
    public void saveKeys(CryptoConfigDto config, Object keys) throws IOException {
        AesKeysDto aesKeys = (AesKeysDto) keys;
        String masterSecretVaultKey = vaultInteractionUtility.getKeyName(config, KeyTypes.AES_MASTER_KEY.getKeyName(), config.getCurrentKeyVersion());
        String ivSecretVaultKey = vaultInteractionUtility.getKeyName(config, KeyTypes.AES_IV_KEY.getKeyName(), config.getCurrentKeyVersion());

        vaultInteractionUtility.setSecret(masterSecretVaultKey, aesKeys.getMasterKey());
        vaultInteractionUtility.setSecret(ivSecretVaultKey, aesKeys.getIv());
    }

    @Override
    public void removeKeys(CryptoConfigDto config, String keyVersion) throws IOException {
        List<String> keyTypes = new ArrayList<>();
        keyTypes.add(KeyTypes.AES_MASTER_KEY.getKeyName());
        keyTypes.add(KeyTypes.AES_IV_KEY.getKeyName());

        List<String> keyNames = keyTypes.stream().map((keyType) -> vaultInteractionUtility.getKeyName(config, keyType, keyVersion)).toList();
        this.removeSecretKey(keyNames);
    }
}
