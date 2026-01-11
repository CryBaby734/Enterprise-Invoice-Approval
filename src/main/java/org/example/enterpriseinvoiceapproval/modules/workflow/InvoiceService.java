package org.example.enterpriseinvoiceapproval.modules.workflow;

import lombok.RequiredArgsConstructor;
import org.example.enterpriseinvoiceapproval.common.InvoiceStatus;
import org.example.enterpriseinvoiceapproval.modules.storage.StorageService;
import org.example.enterpriseinvoiceapproval.modules.workflow.dto.DecisionRequest;
import org.example.enterpriseinvoiceapproval.modules.workflow.events.InvoiceStatusChangedEvent;
import org.example.enterpriseinvoiceapproval.modules.workflow.rules.ApprovalRule;
import org.example.enterpriseinvoiceapproval.repository.InvoiceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final StorageService storageService;
    private final List<ApprovalRule> approvalRules;

    private final ApplicationEventPublisher eventPublisher;

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

        for (ApprovalRule rule : approvalRules) {
            if(rule.apply(invoice)) {
                break;
            }
        }

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public InvoiceEntity processDecision(UUID invoiceId, DecisionRequest decisionRequest) {

        InvoiceEntity invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));


        if (invoice.getStatus() != InvoiceStatus.PENDING_MANAGER) {
            throw new IllegalStateException("Invoice status is already processed! Current status is: " + invoice.getStatus());
        }

        InvoiceStatus oldStatus = invoice.getStatus();

        if(decisionRequest.getStatus() != InvoiceStatus.APPROVED && decisionRequest.getStatus() != InvoiceStatus.REJECTED) {
            throw new IllegalArgumentException("Invalid status for decision");
        }

        invoice.setStatus(decisionRequest.getStatus());
        invoice.setRejectionReason(decisionRequest.getComment());

        InvoiceEntity savedInvoice = invoiceRepository.save(invoice);

        eventPublisher.publishEvent(new InvoiceStatusChangedEvent(
                savedInvoice.getId(),
                savedInvoice.getUserId(),
                oldStatus,
                savedInvoice.getStatus()
        ));



        return savedInvoice;


    }
}
