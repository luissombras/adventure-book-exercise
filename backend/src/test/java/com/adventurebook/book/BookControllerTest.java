package com.adventurebook.book;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void listsBundledBooksSortedByTitle() {
        assertThat(mvc.get().uri("/api/books"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[*].id")
                .asArray()
                .containsExactly("pirates-jade-sea", "crystal-caverns", "the-prisoner");
    }

    @Test
    void exposesValidationAndCardDetails() {
        var prisoner = assertThat(mvc.get().uri("/api/books"))
                .hasStatusOk()
                .bodyJson();

        prisoner.extractingPath("$[2].valid").isEqualTo(true);
        prisoner.extractingPath("$[2].errors").asArray().isEmpty();
        prisoner.extractingPath("$[2].sectionCount").isEqualTo(6);
        prisoner.extractingPath("$[2].description").asString().startsWith("You wake up in what seems to be a dark prison cell");
    }

    @Test
    void invalidBooksExplainWhy() {
        assertThat(mvc.get().uri("/api/books"))
                .bodyJson()
                .extractingPath("$[1].errors")
                .asArray()
                .containsExactly("Section 666 is not an ending but has no options");
    }
}
