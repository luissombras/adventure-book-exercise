package com.adventurebook.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsequenceTest {

    @Test
    void gainingHealthIsAPositiveChange() {
        assertThat(new Consequence(ConsequenceType.GAIN_HEALTH, 3, "Rest").healthChange()).isEqualTo(3);
    }

    @Test
    void losingHealthIsANegativeChange() {
        assertThat(new Consequence(ConsequenceType.LOSE_HEALTH, 6, "Nail").healthChange()).isEqualTo(-6);
    }

    @Test
    void consequenceWithoutATypeOnlyTellsAStory() {
        assertThat(new Consequence(null, 4, "A strange feeling").healthChange()).isZero();
    }
}
