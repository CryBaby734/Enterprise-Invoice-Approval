package org.example.enterpriseinvoiceapproval.modules.workflow;

import lombok.RequiredArgsConstructor;
import org.example.enterpriseinvoiceapproval.Identity.UserEntity;
import org.example.enterpriseinvoiceapproval.common.InvoiceStatus;
import org.example.enterpriseinvoiceapproval.common.Role;
import org.example.enterpriseinvoiceapproval.modules.storage.StorageService;
import org.example.enterpriseinvoiceapproval.modules.workflow.dto.DecisionRequest;
import org.example.enterpriseinvoiceapproval.modules.workflow.events.InvoiceStatusChangedEvent;
import org.example.enterpriseinvoiceapproval.modules.workflow.rules.ApprovalRule;
import org.example.enterpriseinvoiceapproval.repository.InvoiceRepository;
import org.example.enterpriseinvoiceapproval.repository.UserRepository;
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
    private final UserRepository userRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public InvoiceEntity createInvoice(MultipartFile file, String vendorName, BigDecimal amount,String userEmail, String userFullName) {

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseGet(()-> {
                    UserEntity newUser = UserEntity.builder()
                            .email(userEmail)
                            .fullName(userFullName)
                            .role(Role.EMPLOYEE)
                            .active(true)
                            .build();
                            return userRepository.save(newUser);
                });


        String s3Key = storageService.uploadFile(file);

        InvoiceEntity invoice = InvoiceEntity.builder()
                .userId(user.getId())
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
