package org.example.enterpriseinvoiceapproval.modules.workflow;

import jakarta.persistence.*;
import lombok.*;
import org.example.enterpriseinvoiceapproval.common.InvoiceStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "vendor_name", nullable = false)
    private String vendorName;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "EUR";

    private String description;

    @Column(name = "s3_file_key", nullable = false)
    private String s3FileKey;

    @Column(name = "content_hash")
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Version
    private Long version;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
