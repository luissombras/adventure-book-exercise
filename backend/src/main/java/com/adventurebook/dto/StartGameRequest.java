package com.adventurebook.dto;

import jakarta.validation.constraints.NotBlank;

public record StartGameRequest(@NotBlank String bookId) {
}
