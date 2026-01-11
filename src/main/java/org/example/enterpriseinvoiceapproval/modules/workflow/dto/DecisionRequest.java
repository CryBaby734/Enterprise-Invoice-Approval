package org.example.enterpriseinvoiceapproval.modules.workflow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.enterpriseinvoiceapproval.common.InvoiceStatus;

@Data
public class DecisionRequest {

    @NotNull
    private InvoiceStatus status;
    private String comment;
}
