package com.example.esee_poc.repo;

import com.example.esee_poc.entity.CryptoConfig;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CryptoConfigRepo extends JpaRepository<CryptoConfig, UUID> {
    public Optional<CryptoConfig> findFirstByPartnerIdAndClientIdAndIsDeletedFalseOrderByCreatedAtDesc(String partnerId, String clientId);

    @Modifying
    @Transactional
    @Query("DELETE FROM client_partner_crypto_config e WHERE e.clientId = :clientId AND e.partnerId = :partnerId AND e.currentKeyVersion = :keyVersion AND e.isDeleted = false")
    public void deleteByClientIdAndPartnerIdAndCurrentKeyVersionAndIsDeletedFalse(@Param("clientId") String clientId,
                                                                          @Param("partnerId") String partnerId,
                                                                          @Param("keyVersion") String keyVersion);
}
