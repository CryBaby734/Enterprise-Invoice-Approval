package org.example.enterpriseinvoiceapproval.controllers;

import lombok.RequiredArgsConstructor;
import org.example.enterpriseinvoiceapproval.modules.workflow.InvoiceEntity;
import org.example.enterpriseinvoiceapproval.modules.workflow.InvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InvoiceEntity> uploadInvoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam("vendorName") String vendorName,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("userId") UUID userId
    ){
        InvoiceEntity invoice = invoiceService.createInvoice(file, vendorName, amount, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }
}
