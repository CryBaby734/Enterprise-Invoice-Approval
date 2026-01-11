package org.example.enterpriseinvoiceapproval;

import org.springframework.boot.SpringApplication;

public class TestEnterpriseInvoiceApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.from(EnterpriseInvoiceApprovalApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
