package fr.exalt.bankaccount.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal value) {
    public Money {
        Objects.requireNonNull(value);
        value = value.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Money of(String s) {
        return new Money(new BigDecimal(s));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public boolean isLessThanOrEqual(Money zero) {
        return (this.value.compareTo(zero.value()) < 0 || this.value.compareTo(zero.value()) == 0);
    }

    public Money add(Money other) {
        return new Money(this.value().add(other.value()));
    }
}
