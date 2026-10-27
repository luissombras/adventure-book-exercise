package com.adventurebook.dto;

import jakarta.validation.constraints.NotNull;

public record ChoiceRequest(@NotNull Integer optionIndex) {
}
