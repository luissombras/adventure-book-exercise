package com.adventurebook.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void startsAGame() {
        MvcTestResult result = start("the-prisoner");

        assertThat(result).hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/games/" + gameId(result));
        assertThat(result).bodyJson().extractingPath("$.section.id").isEqualTo(1);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo("IN_PROGRESS");
        assertThat(result).bodyJson().extractingPath("$.health").isEqualTo(10);
        assertThat(result).bodyJson().extractingPath("$.consequence").isNull();
    }

    @Test
    void reportsTheConsequenceOfAChoice() {
        String gameId = gameId(start("the-prisoner"));
        choose(gameId, "{\"optionIndex\": 1}");

        var body = assertThat(choose(gameId, "{\"optionIndex\": 0}")).hasStatusOk().bodyJson();

        body.extractingPath("$.health").isEqualTo(4);
        body.extractingPath("$.consequence.healthChange").isEqualTo(-6);
    }

    @Test
    void makesAChoiceAndReturnsTheNewSection() {
        String gameId = gameId(start("the-prisoner"));

        assertThat(choose(gameId, "{\"optionIndex\": 1}"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.section.id")
                .isEqualTo(20);
        assertThat(mvc.get().uri("/api/games/{id}", gameId))
                .bodyJson()
                .extractingPath("$.section.id")
                .isEqualTo(20);
    }

    @Test
    void unplayableBookIsUnprocessableAndListsTheErrors() {
        MvcTestResult result = start("crystal-caverns");

        assertThat(result).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.errors")
                .asArray()
                .containsExactly("Section 666 is not an ending but has no options");
    }

    @Test
    void unknownGameIsNotFound() {
        assertThat(mvc.get().uri("/api/games/missing"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .extractingPath("$.detail")
                .isEqualTo("Game missing not found");
    }

    @Test
    void unavailableOptionIsAConflict() {
        String gameId = gameId(start("the-prisoner"));

        assertThat(choose(gameId, "{\"optionIndex\": 5}")).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void malformedRequestsAreBadRequests() {
        assertThat(mvc.post().uri("/api/games").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(choose(gameId(start("the-prisoner")), "{}")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    private MvcTestResult start(String bookId) {
        return mvc.post().uri("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookId\": \"" + bookId + "\"}")
                .exchange();
    }

    private MvcTestResult choose(String gameId, String body) {
        return mvc.post().uri("/api/games/{id}/choices", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();
    }

    private static String gameId(MvcTestResult result) {
        return JsonPath.read(new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8), "$.id");
    }
}
