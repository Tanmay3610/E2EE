package com.example.esee_poc.repo;

import com.example.esee_poc.entity.CryptoConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CryptoConfigRepo extends JpaRepository<CryptoConfig, UUID> {
    public Optional<CryptoConfig> findFirstByPartnerIdAndClientIdAndIsDeletedFalseOrderByCreatedAtDesc(String partnerId, String clientId);
}
