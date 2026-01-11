package org.example.enterpriseinvoiceapproval.modules.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterpriseinvoiceapproval.modules.workflow.events.InvoiceStatusChangedEvent;
import org.example.enterpriseinvoiceapproval.repository.AuditLogRepository;
import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvoiceStatusChanged(InvoiceStatusChangedEvent event) {
        log.info("Audit module received event for invoice: {}", event.invoiceId());

        AuditLogEntity auditLog = AuditLogEntity.builder()
                .invoiceId(event.invoiceId())
                .actorId(event.actorId())
                .action("STATUS_CHANGE")
                .oldStatus(event.oldstatus().name())
                .newStatus(event.newstatus().name())
                .comment("Status changed via workflow").build();

        auditLogRepository.save(auditLog);
    }

}
