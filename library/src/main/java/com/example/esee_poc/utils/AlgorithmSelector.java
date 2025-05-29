package com.example.esee_poc.utils;

import com.example.esee_poc.interfaces.Algorithm;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AlgorithmSelector {
    private final Map<String, Algorithm<?>> algorithmMap;

    public AlgorithmSelector(Map<String, Algorithm<?>> algorithmMap) {
        this.algorithmMap = algorithmMap;
    }

    public Algorithm<?> getAlgorithm(String algorithm) {
        return algorithmMap.get(algorithm);
    }
}
