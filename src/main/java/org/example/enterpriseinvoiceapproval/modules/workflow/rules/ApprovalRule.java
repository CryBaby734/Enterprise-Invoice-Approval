package org.example.enterpriseinvoiceapproval.modules.workflow.rules;


import org.example.enterpriseinvoiceapproval.modules.workflow.InvoiceEntity;

public interface ApprovalRule {

    boolean apply (InvoiceEntity invoice);
}
