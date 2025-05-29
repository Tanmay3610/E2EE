package com.example.bank_sys.controller;

import com.example.bank_sys.dto.EncryptDataRequestDto;
import com.example.bank_sys.dto.EncryptedKeysDto;
import com.example.bank_sys.services.PartnerService;
import com.example.esee_poc.Constants;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/partner/api")
public class PartnerController {
    @Autowired
    PartnerService partnerService;

    @PostMapping("/encrypt-payload")
    public ResponseEntity<String> encryptPayload(
            @Valid
            @RequestHeader(Constants.PARTNER_ID_HEADER_KEY) String partnerId,
            @Valid
            @RequestHeader(Constants.CLIENT_ID_HEADER_KEY) String clientId
    ) throws Exception {
        String res = partnerService.encryptPayload(partnerId, clientId);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/encrypt-keys")
    public ResponseEntity<EncryptedKeysDto> encryptKeys(
            @Valid
            @RequestHeader(Constants.PARTNER_ID_HEADER_KEY) String partnerId,
            @Valid
            @RequestHeader(Constants.CLIENT_ID_HEADER_KEY) String clientId,
            @Valid
            @RequestHeader(Constants.CLIENT_KEY_VERSION) String keyVersion
            ) throws Exception {
        EncryptedKeysDto res = partnerService.encryptKeys(partnerId, clientId, keyVersion);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/decrypt-data")
    public ResponseEntity<String> decryptData(
            @Valid
            @RequestHeader(Constants.PARTNER_ID_HEADER_KEY) String partnerId,
            @Valid
            @RequestHeader(Constants.CLIENT_ID_HEADER_KEY) String clientId,
            @RequestBody EncryptDataRequestDto request) throws Exception {
        String res = partnerService.decryptData(partnerId, clientId, request);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/start-session")
    public ResponseEntity<String> startSession(
            @Valid
            @RequestHeader(Constants.PARTNER_ID_HEADER_KEY) String partnerId,
            @Valid
            @RequestHeader(Constants.CLIENT_ID_HEADER_KEY) String clientId
    ) throws IOException, NoSuchAlgorithmException {
        String res = partnerService.startSession(partnerId, clientId);
        return ResponseEntity.ok(res);
    }
}
