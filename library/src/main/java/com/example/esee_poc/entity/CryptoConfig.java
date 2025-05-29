package com.example.esee_poc.entity;

import com.example.esee_poc.enums.AlgorithmTypes;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "client_partner_crypto_config")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CryptoConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "config_id")
    UUID configId;

    @Column(name = "partner_id")
    String partnerId;

    @Column(name = "partner_algorithm")
    AlgorithmTypes partnerAlgorithm;

    @Column(name = "client_algorithm")
    AlgorithmTypes clientAlgorithm;

    @Column(name = "client_id")
    String clientId;

    @Column(name = "current_key_version")
    String currentKeyVersion;

    @Column(name = "is_deleted")
    boolean isDeleted;

    @Column(name = "created_at")
    Instant createdAt;
}
