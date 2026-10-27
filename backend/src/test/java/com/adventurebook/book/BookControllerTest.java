package com.adventurebook.book;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
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
        var body = assertThat(mvc.get().uri("/api/books").param("search", "prisoner"))
                .hasStatusOk()
                .bodyJson();

        body.extractingPath("$[0].valid").isEqualTo(true);
        body.extractingPath("$[0].errors").asArray().isEmpty();
        body.extractingPath("$[0].sectionCount").isEqualTo(6);
        body.extractingPath("$[0].description").asString().startsWith("You wake up in what seems to be a dark prison cell");
    }

    @Test
    void invalidBooksExplainWhy() {
        assertThat(mvc.get().uri("/api/books").param("search", "crystal"))
                .bodyJson()
                .extractingPath("$[0].errors")
                .asArray()
                .containsExactly("Section 666 is not an ending but has no options");
    }

    @Test
    void filtersByDifficulty() {
        assertThat(mvc.get().uri("/api/books").param("difficulty", "EASY", "HARD"))
                .bodyJson()
                .extractingPath("$[*].id")
                .asArray()
                .containsExactly("crystal-caverns", "the-prisoner");
    }

    @Test
    void rejectsUnknownDifficulty() {
        assertThat(mvc.get().uri("/api/books").param("difficulty", "IMPOSSIBLE"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }
}
