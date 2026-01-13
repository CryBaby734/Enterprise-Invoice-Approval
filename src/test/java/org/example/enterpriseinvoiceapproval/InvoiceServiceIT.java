package org.example.enterpriseinvoiceapproval;

import org.example.enterpriseinvoiceapproval.common.InvoiceStatus;
import org.example.enterpriseinvoiceapproval.common.Role;
import org.example.enterpriseinvoiceapproval.Identity.UserEntity;
import org.example.enterpriseinvoiceapproval.modules.workflow.InvoiceEntity;
import org.example.enterpriseinvoiceapproval.modules.workflow.InvoiceService;
import org.example.enterpriseinvoiceapproval.repository.InvoiceRepository;
import org.example.enterpriseinvoiceapproval.repository.UserRepository;
import org.junit.jupiter.api.Test; // <--- ВАЖНО: Используем Jupiter (JUnit 5)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

// Убедись, что AbstractIntegrationTest находится в том же пакете или импортирован
public class InvoiceServiceIT extends AbstractIntegrationTest {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateInvoiceAndUploadFile() {
        // GIVEN
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-invoice.pdf",
                "application/pdf",
                "Hello World Content".getBytes(StandardCharsets.UTF_8));

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .fullName("Test User")
                .role(Role.EMPLOYEE) // Changed USER to EMPLOYEE
                .active(true)
                .build();
        userRepository.save(user);

        UUID userId = user.getId();
        BigDecimal amount = new BigDecimal("150.00");

        // WHEN
        // Исправлено: вызываем у переменной invoiceService (с маленькой буквы)
        InvoiceEntity result = invoiceService.createInvoice(file, "Test Vendor", amount, userId);

        // THEN
        assertThat(result.getId()).isNotNull();
        assertThat(result.getS3FileKey()).contains("test-invoice.pdf");

        // Проверяем Rule Engine (150 < 500 -> APPROVED)
        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.APPROVED);

        // Тут можно поправить текст, если в SmallAmountRule он отличается
        assertThat(result.getRejectionReason()).contains("Auto-approved");

        // Проверяем БД
        InvoiceEntity savedInDb = invoiceRepository.findById(result.getId()).orElseThrow();
        assertThat(savedInDb.getVendorName()).isEqualTo("Test Vendor");
    }
}