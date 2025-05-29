package com.example.esee_poc.utils.algorithms;

import com.example.esee_poc.Constants;
import com.example.esee_poc.dto.CryptoConfigDto;
import com.example.esee_poc.dto.keys.RSAKeysDto;
import com.example.esee_poc.interfaces.Algorithm;
import com.example.esee_poc.interfaces.VaultInteractionUtility;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.esee_poc.enums.KeyTypes;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component(Constants.RSA_ALGORITHM_BEAN_NAME)
public class RSA extends Algorithm<RSAKeysDto> {
    @Autowired
    private VaultInteractionUtility vaultInteractionUtility;

    private static final String RSA_ALGORITHM = "RSA";

    private PublicKey getPublicKey(String base64PublicKey) throws GeneralSecurityException, NoSuchAlgorithmException {
        byte[] decoded = Base64.getDecoder().decode(base64PublicKey);
        System.out.println("Decoded Public Key Length: " + decoded.length);
        EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        System.out.println("Decoded Public Key: " + Base64.getEncoder().encodeToString(decoded));
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(spec);
    }

    private PrivateKey getPrivateKey(String base64PrivateKey) throws GeneralSecurityException {
        byte[] decoded = Base64.getDecoder().decode(base64PrivateKey);
        EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(spec);
    }

    @Override
    public String encrypt(String data, List<String> keys) throws GeneralSecurityException {
        String base64PublicKey = keys.getFirst();
        PublicKey publicKey = getPublicKey(base64PublicKey);
        System.out.println("Fetched Public Key: " + publicKey);
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    @Override
    public String decrypt(String data, List<String> keys) throws GeneralSecurityException {
        String base64PrivateKey = keys.get(1);
        PrivateKey privateKey = getPrivateKey(base64PrivateKey);

        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(data));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    @Override
    public RSAKeysDto generateKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGenerator.initialize(2048);  // You can choose 2048 or 4096 bits
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        System.out.println("Public Key Length" + keyPair.getPublic().getEncoded().length);
        byte[] publicKeyStr = keyPair.getPublic().getEncoded();
        byte[] privateKeyStr = keyPair.getPrivate().getEncoded();

        return RSAKeysDto.builder().publicKey(publicKeyStr).privateKey(privateKeyStr).build();
    }

    @Override
    public void saveKeys(CryptoConfigDto config, Object keys) throws IOException {
        RSAKeysDto keyPair = (RSAKeysDto) keys;
        byte[] publicKey = keyPair.getPublicKey();
        byte[] privateKey = keyPair.getPrivateKey();

        String publicSecretVaultKey = vaultInteractionUtility.getKeyName(config, KeyTypes.RSA_PUBLIC_KEY.getKeyName(), config.getCurrentKeyVersion());
        String privateSecretVaultKey = vaultInteractionUtility.getKeyName(config, KeyTypes.RSA_PRIVATE_KEY.getKeyName(), config.getCurrentKeyVersion());

        vaultInteractionUtility.setSecret(publicSecretVaultKey, publicKey);
        vaultInteractionUtility.setSecret(privateSecretVaultKey, privateKey);
    }

    @Override
    public void removeKeys(CryptoConfigDto config, String keyVersion) throws IOException {
        List<String> keyTypes = new ArrayList<>();
        keyTypes.add(KeyTypes.RSA_PUBLIC_KEY.getKeyName());
        keyTypes.add(KeyTypes.RSA_PRIVATE_KEY.getKeyName());

        List<String> keyNames = keyTypes.stream().map((keyType) -> vaultInteractionUtility.getKeyName(config, keyType, keyVersion)).toList();
        this.removeSecretKey(keyNames);
    }

    @Override
    public List<String> fetchKey(CryptoConfigDto config, String keyVersion) throws Exception {
        List<String> keyTypes = new ArrayList<>();
        keyTypes.add(KeyTypes.RSA_PUBLIC_KEY.getKeyName());
        keyTypes.add(KeyTypes.RSA_PRIVATE_KEY.getKeyName());

        return this.fetchSecretKey(config, keyTypes, keyVersion);
    }
}
