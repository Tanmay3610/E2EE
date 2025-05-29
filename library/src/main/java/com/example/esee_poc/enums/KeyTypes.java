package com.example.esee_poc.enums;

import lombok.Getter;

@Getter
public enum KeyTypes {
    AES_MASTER_KEY("aes_master_key"),
    AES_IV_KEY("aes_iv_key"),

    RSA_PRIVATE_KEY("rsa_private_key"),
    RSA_PUBLIC_KEY("rsa_public_key");

    private final String keyName;

    KeyTypes(String keyName) {
        this.keyName = keyName;
    }
}
