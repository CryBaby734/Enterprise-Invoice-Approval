package org.example.enterpriseinvoiceapproval.modules.workflow;

import lombok.RequiredArgsConstructor;
import org.example.enterpriseinvoiceapproval.common.InvoiceStatus;
import org.example.enterpriseinvoiceapproval.modules.storage.StorageService;
import org.example.enterpriseinvoiceapproval.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final StorageService storageService;

    @Transactional
    public InvoiceEntity createInvoice(MultipartFile file, String vendorName, BigDecimal amount, UUID userId) {

        String s3Key = storageService.uploadFile(file);

        InvoiceEntity invoice = InvoiceEntity.builder()
                .userId(userId)
                .vendorName(vendorName)
                .amount(amount)
                .s3FileKey(s3Key)
                .status(InvoiceStatus.PENDING_MANAGER)
                .build();

        return invoiceRepository.save(invoice);
    }
}
