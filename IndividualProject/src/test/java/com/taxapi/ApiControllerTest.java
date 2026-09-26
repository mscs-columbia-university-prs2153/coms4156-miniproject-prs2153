package com.taxapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
@Import(TestConfig.class)
class ApiControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private LocalStorageService localStorageService;

    private MockMvc mockMvc;

    @TempDir
    Path tempDir;

    protected static final String VALID_KEY = "valid-key";

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        Files.writeString(tempDir.resolve("clients.json"),
            "[{\"id\":\"client-1\",\"name\":\"Alice\",\"apiKey\":\"valid-key\"}]");
        Files.writeString(tempDir.resolve("items.json"),
            "[{\"id\":\"item-1\",\"name\":\"Laptop\",\"category\":\"electronics\",\"basePrice\":999.99}]");
        Files.writeString(tempDir.resolve("taxrates.json"),
            "[{\"state\":\"CA\",\"category\":\"electronics\",\"rate\":0.0725},"
            + "{\"state\":\"NY\",\"category\":\"clothing\",\"rate\":0.04}]");

        localStorageService.setDirectory(tempDir);
    }

    // TODO(student): add @Test methods that exercise ApiController endpoints
    // via mockMvc.perform(...). Aim for >= 55% JaCoCo coverage overall.

    @Test
    void contextLoads() {
        // Placeholder so the test class is non-empty. Replace with real tests.
    }

    @Test
    void postNewClient() throws Exception {
        mockMvc.perform(
            post("/v1/clients")
            .contentType("application/json")
            .content("{\"name\":\"Patrick\"}")
        )
        .andExpect(status().isOk());
    }

    @Test
    void postExistinglient() throws Exception {
        mockMvc.perform(
            post("/v1/clients")
            .contentType("application/json")
            .content("{\"name\":\"Alice\"}")
        )
        .andExpect(status().isConflict());
    }

    @Test
    void postNewItem() throws Exception {
        mockMvc.perform(
            post("/v1/items")
            .header("X-API-Key", VALID_KEY)
            .contentType("application/json")
            .content("{\"name\":\"Candy\",\"category\":\"electronics\",\"basePrice\":1.99}")
        )
        .andExpect(status().isOk());
    }

    @Test
    void postNewItemWithInvalidApiKey() throws Exception {
        mockMvc.perform(
            post("/v1/items")
            .header("X-API-Key", "")
            .contentType("application/json")
            .content("{\"name\":\"Candy\",\"category\":\"electronics\",\"basePrice\":1.99}")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void getItems() throws Exception {
        mockMvc.perform(
            get("/v1/items")
            .header("X-API-Key", VALID_KEY)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("item-1"))
        .andExpect(jsonPath("$[0].category").value("electronics"))
        .andExpect(jsonPath("$[0].basePrice").value("999.99"));
    }

    @Test
    void getItemsWithInvalidApiKey() throws Exception {
        mockMvc.perform(
            get("/v1/items")
            .header("X-API-Key", "")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void getItemsWithName() throws Exception {
        mockMvc.perform(
            get("/v1/items?q=lap")
            .header("X-API-Key", VALID_KEY)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("item-1"))
        .andExpect(jsonPath("$[0].category").value("electronics"))
        .andExpect(jsonPath("$[0].basePrice").value("999.99"));
    }

    @Test
    void getItemsWithCategory() throws Exception {
        mockMvc.perform(
            get("/v1/items?category=electronics")
            .header("X-API-Key", VALID_KEY)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("item-1"))
        .andExpect(jsonPath("$[0].category").value("electronics"))
        .andExpect(jsonPath("$[0].basePrice").value("999.99"));
    }

    @Test
    void getItemsWithNameAndCategory() throws Exception {
        mockMvc.perform(
            get("/v1/items?q=lap&category=electronics")
            .header("X-API-Key", VALID_KEY)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("item-1"))
        .andExpect(jsonPath("$[0].category").value("electronics"))
        .andExpect(jsonPath("$[0].basePrice").value("999.99"));
    }

    @Test
    void getItemsWithNameAndCategoryDoesntExist() throws Exception {
        mockMvc.perform(
            get("/v1/items?q=car&category=vehicles")
            .header("X-API-Key", VALID_KEY)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getItemsById() throws Exception {
        mockMvc.perform(
            get("/v1/items/item-1")
            .header("X-API-Key", VALID_KEY)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("category").value("electronics"))
        .andExpect(jsonPath("basePrice").value("999.99"));
    }

    @Test
    void getItemsByIdWithInvalidApiKey() throws Exception {
        mockMvc.perform(
            get("/v1/items/Laptop")
            .header("X-API-Key", "")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteItemsByIdWithInvalidApiKey() throws Exception {
        mockMvc.perform(
            delete("/v1/items/item-1")
            .header("X-API-Key", "")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteItemsById() throws Exception {
        mockMvc.perform(
            delete("/v1/items/item-1")
            .header("X-API-Key", VALID_KEY)
        )
        .andExpect(status().isNoContent());
    }

    @Test
    void updateItemBasePrice() throws Exception {
        mockMvc.perform(
            patch("/v1/items/item-1")
            .header("X-API-Key", VALID_KEY)
            .header("newPrice", 99.99)
        )
        .andExpect(status().isOk());

        // check price changed for following request
        mockMvc.perform(
            get("/v1/items/item-1")
            .header("X-API-Key", VALID_KEY)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("basePrice").value("99.99"));
    }

    @Test
    void updateItemBasePriceWithInvalidApiKey() throws Exception {
        mockMvc.perform(
            patch("/v1/items/item-1")
            .header("X-API-Key", "")
            .header("newPrice", 99.99)
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void updateItemBasePriceWithZeroPrice() throws Exception {
        mockMvc.perform(
            patch("/v1/items/item-1")
            .header("X-API-Key", VALID_KEY)
            .header("newPrice", 0)
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Price has to be positive and non-zero."));

        // check price DID'NT changed for following request
        mockMvc.perform(
            get("/v1/items/item-1")
            .header("X-API-Key", VALID_KEY)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("basePrice").value("999.99"));
    }

    @Test
    void calculateTaxWithoutId() throws Exception {
        mockMvc.perform(
            post("/v1/tax/quote")
            .header("X-API-Key", VALID_KEY)
            .contentType("application/json")
            .content("{\"price\":\"100.00\", \"category\":\"electronics\", \"state\":\"CA\"}")
        )
        .andExpect(status().isOk());
    }

    @Test
    void calculateTaxWithInvalidApiKey() throws Exception {
        mockMvc.perform(
            post("/v1/tax/quote")
            .header("X-API-Key", "")
            .contentType("application/json")
            .content("{}")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void getSupported() throws Exception {
        mockMvc.perform(
            get("/v1/supported")
            .header("X-API-Key", VALID_KEY)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.states[0]").value("CA"))
        .andExpect(jsonPath("$.categories[0]").value("electronics"))
        .andExpect(jsonPath("$.states[1]").value("NY"))
        .andExpect(jsonPath("$.categories[1]").value("clothing"));
    }

    @Test
    void getSupportedWithInvalidApiKey() throws Exception {
        mockMvc.perform(
            get("/v1/supported")
            .header("X-API-Key", "")
        )
        .andExpect(status().isUnauthorized());
    }
}
