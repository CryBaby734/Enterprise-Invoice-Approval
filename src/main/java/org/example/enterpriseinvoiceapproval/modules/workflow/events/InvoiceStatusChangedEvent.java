package org.example.enterpriseinvoiceapproval.modules.workflow.events;

import org.example.enterpriseinvoiceapproval.common.InvoiceStatus;

import java.util.UUID;

public record InvoiceStatusChangedEvent(
        UUID invoiceId,
        UUID actorId,
        InvoiceStatus oldstatus,
        InvoiceStatus newstatus
) {
}
