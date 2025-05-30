package com.example.bank_sys.controller;

import com.example.esee_poc.dto.CryptoConfigDto;
import com.example.bank_sys.services.CryptoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/crypto")
public class CryptoController {
    @Autowired
    private CryptoService cryptoService;

    @GetMapping("/crypto-config/{partnerId}/{clientId}")
    public ResponseEntity<CryptoConfigDto> getCryptoConfig(
            @PathVariable
            @Valid
            String partnerId,
            @PathVariable
            @Valid
            String clientId
    ) {
        CryptoConfigDto cryptoConfig = cryptoService.getCryptoConfig(partnerId, clientId);
        return ResponseEntity.ok(cryptoConfig);
    }

    @PostMapping("/onboard-client-partner")
    public ResponseEntity<String> onboardClient(@RequestBody CryptoConfigDto cryptoConfig) {
        String res = cryptoService.onboardClient(cryptoConfig);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/rotate-keys/{partnerId}/{clientId}")
    public ResponseEntity<String> rotateKeys(
            @PathVariable
            @Valid
            String partnerId,
            @PathVariable
            @Valid
            String clientId
    ) throws Exception {
        String res = cryptoService.rotateKeys(clientId, partnerId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/delete-key/{partnerId}/{clientId}/{keyVersion}")
    public ResponseEntity<String> deleteKeys(
            @PathVariable
            @Valid
            String partnerId,
            @PathVariable
            @Valid
            String clientId,
            @PathVariable
            @Valid
            String keyVersion
    ) throws Exception {
        String res = cryptoService.deleteKey(clientId, partnerId, keyVersion);
        return ResponseEntity.ok(res);
    }
}
