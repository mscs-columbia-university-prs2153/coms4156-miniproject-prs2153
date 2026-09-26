package com.taxapi;

import com.taxapi.service.TaxApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.taxapi.model.Client;
import com.taxapi.model.TaxQuoteRequest;
import com.taxapi.model.TaxQuoteResponse;


import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
@Import(TestConfig.class)
class TaxApiServiceUnitTests {

    @Autowired
    private TaxApiService service;

    @Autowired
    private LocalStorageService localStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        Files.writeString(tempDir.resolve("clients.json"),
            "[{\"id\":\"client-1\",\"name\":\"Alice\",\"apiKey\":\"valid-key\"}]");
        Files.writeString(tempDir.resolve("items.json"),
            "[{\"id\":\"item-1\",\"name\":\"Laptop\",\"category\":\"electronics\",\"basePrice\":999.99}]");
        Files.writeString(tempDir.resolve("taxrates.json"),
            "[{\"state\":\"CA\",\"category\":\"electronics\",\"rate\":0.0725},"
            + "{\"state\":\"NY\",\"category\":\"clothing\",\"rate\":0.04}]");

        localStorageService.setDirectory(tempDir);
    }

    // TODO(student): add @Test methods that exercise TaxApiService directly.
    // The `service` field above is the autowired bean under test.

    @Test
    void contextLoads() {
        // Placeholder so the test class is non-empty. Replace with real tests.
    }

    @Test
    void calculateTaxByItemId() throws Exception {
        TaxQuoteRequest tqr = new TaxQuoteRequest();
        tqr.setItemId("item-1");
        tqr.setState("CA");

        TaxQuoteResponse result = service.calculateTax(tqr);
        assertEquals(result.getPrice(), 999.99);
    }

    @Test
    void calculateTaxByNonexistentItemId() throws Exception {
        TaxQuoteRequest tqr = new TaxQuoteRequest();
        tqr.setItemId("item-100");
        tqr.setState("CA");

        TaxQuoteResponse result = service.calculateTax(tqr);
        assertEquals(result, null);
    }

    @Test
    void calculateTaxWithoutItemId() throws Exception {
        TaxQuoteRequest tqr = new TaxQuoteRequest();
        tqr.setCategory("electronics");
        tqr.setPrice(1.00);
        tqr.setState("CA");

        TaxQuoteResponse result = service.calculateTax(tqr);
        assertEquals(result.getPrice(), 1.00);
        assertEquals(result.getTaxRate(), 0.0725);
        assertEquals(result.getTaxAmount(), 0.0725);
        assertEquals(result.getTotal(), 1.0725);

    }

    @Test
    void calculateTaxByNonexistentState() throws Exception {
        TaxQuoteRequest tqr = new TaxQuoteRequest();
        tqr.setCategory("electronics");
        tqr.setPrice(1.00);
        tqr.setState("AA");

        TaxQuoteResponse result = service.calculateTax(tqr);
        assertEquals(result, null);
    }

    @Test
    void calculateTaxByNonexistentCategory() throws Exception {
        TaxQuoteRequest tqr = new TaxQuoteRequest();
        tqr.setCategory("cars");
        tqr.setPrice(1.00);
        tqr.setState("CA");

        TaxQuoteResponse result = service.calculateTax(tqr);
        assertEquals(result, null);
    }
}
