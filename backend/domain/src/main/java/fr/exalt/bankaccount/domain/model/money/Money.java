package fr.exalt.bankaccount.domain.model.money;

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

    public boolean isLessThanOrEqual(Money other) {
        return (this.value.compareTo(other.value()) <= 0);
    }

    public boolean greaterThanOrEqual(Money other) {
        return (this.value.compareTo(other.value()) >= 0);
    }

    public boolean isGreaterThan(Money other) {
        return (this.value.compareTo(other.value()) > 0);
    }

    public boolean isLessThan(Money other) {
        return (this.value.compareTo(other.value()) < 0);
    }

    public Money add(Money other) {
        return new Money(this.value().add(other.value()));
    }


    public Money subtract(Money other) {
        return new Money((this.value().subtract(other.value())));
    }


}
