package com.example.esee_poc.enums;

import com.example.esee_poc.Constants;
import lombok.Getter;

@Getter
public enum AlgorithmTypes {
    RSA(Constants.RSA_ALGORITHM_BEAN_NAME),
    AES_256_CBC(Constants.AES_ALGORITHM_BEAN_NAME);

    private final String algorithm;

    AlgorithmTypes(String s) {
        this.algorithm = s;
    }
}
