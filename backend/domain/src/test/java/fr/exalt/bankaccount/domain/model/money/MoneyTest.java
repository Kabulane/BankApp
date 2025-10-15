package fr.exalt.bankaccount.domain.model.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires pour le value object {@link Money}.
 * <p>
 * Ces tests valident la création, la comparaison, et les opérations élémentaires
 * nécessaires aux politiques de dépôt et de découvert.
 * </p>
 */
public class MoneyTest {

    /**
     * Vérifie que {@link Money#of(String)} normalise le nombre de décimales
     * à deux chiffres avec un arrondi bancaire (HALF_EVEN).
     */
    @Test
    @DisplayName("Normalise l'échelle à 2 décimales avec arrondi HALF_EVEN")
    void of_parses_string_and_normalizes_scale_to_2_with_bankers_rounding() {
        assertThat(Money.of("10")).isEqualTo(Money.of("10.00"));
        assertThat(Money.of("1.005")).isEqualTo(Money.of("1.00")); // arrondi 1.005 -> 1.00
        assertThat(Money.of("1.015")).isEqualTo(Money.of("1.02")); // arronde 1.015 -> 1.02
    }

    @Test
    @DisplayName("Normalise l'échelle à 2 décimales avec arrondi HALF_EVEN")
    void of_value_always_positive() {
        assertThat(Money.of("-10")).isEqualTo(Money.of("-10.00"));
        assertThat(Money.of("1.005")).isEqualTo(Money.of("1.00"));
        assertThat(Money.of("1.015")).isEqualTo(Money.of("1.02"));
    }

    /**
     * Vérifie que {@link Money#zero()} retourne un objet représentant 0.00
     * et qu'il est équivalent à {@link Money#of(String)} avec "0.00".
     */
    @Test
    @DisplayName("Retourne une instance 0.00 cohérente avec Money.of(\"0.00\"")
    void zero_factory_is_00_and_cached() {
        Money z1 = Money.zero();
        Money z2 = Money.of("0.00");
        assertThat(z1).isEqualTo(z2);
    }

    /**
     * Vérifie que {@link Money#isLessThanOrEqual(Money)} compare correctement
     * des montants inférieurs, égaux ou supérieurs.
     * <p>
     *     Cette méthode sera utilisée par les politiques de plafonds et de retraits.
     * </p>
     */
    @Test
    @DisplayName("Est inférieur ou égale à")
    void comparison_lte_works() {
        Money m1 = Money.of("0.00");
        Money m2 = Money.of("0.01");
        Money m3 = Money.of("0.015");
        Money m4 = Money.of("0.0153");

        assertThat(m1.isLessThanOrEqual(m2)).isTrue();
        assertThat(m2.isLessThanOrEqual(m1)).isFalse();
        assertThat(m3.isLessThanOrEqual(m4)).isTrue();
        assertThat(m2.isLessThanOrEqual(m3)).isTrue();
    }

    /**
     * Vérifie que {@link Money#greaterThanOrEqual(Money)} compare correctement
     * des montants inférieurs, égaux ou supérieurs.
     * <p>
     * Cette méthode sera utilisée par les politiques de découvert (overdraft policy).
     * </p>
     */
    @Test
    @DisplayName("compare correctement avec greaterThanOrEqual()")
    void comparison_gte_works() {
        Money m1 = Money.of("0.00");
        Money m2 = Money.of("0.01");
        Money m3 = Money.of("0.015");
        Money m4 = Money.of("0.0153");

        assertThat(m1.greaterThanOrEqual(m2)).isFalse();
        assertThat(m2.greaterThanOrEqual(m1)).isTrue();
        assertThat(m2.greaterThanOrEqual(m3)).isFalse();
        assertThat(m3.greaterThanOrEqual(m4)).isTrue();
        assertThat(Money.zero().greaterThanOrEqual(m1)).isTrue();
    }

    @Test
    @DisplayName("Compare correctement avec isLessThan")
    void comparison_lt_works() {
        Money m1 = Money.of("0.00");
        Money m2 = Money.of("0.01");
        Money m3 = Money.of("0.015");
        Money m4 = Money.of("0.0153");

        assertThat(m2.isLessThan(m1)).isFalse();
        assertThat(m1.isLessThan(m2)).isTrue();
        assertThat(m3.isLessThan(m2)).isFalse();
        assertThat(m4.isLessThan(m3)).isFalse();
        assertThat(Money.zero().isLessThan(m1)).isFalse();
    }

    @Test
    @DisplayName("Compare correctement avec isGreaterThan")
    void comparison_gt_works() {
        Money m1 = Money.of("0.00");
        Money m2 = Money.of("0.01");
        Money m3 = Money.of("0.015");
        Money m4 = Money.of("0.0153");

        assertThat(m1.isGreaterThan(m2)).isFalse();
        assertThat(m2.isGreaterThan(m1)).isTrue();
        assertThat(m2.isGreaterThan(m3)).isFalse();
        assertThat(m3.isGreaterThan(m4)).isFalse();
        assertThat(Money.zero().isGreaterThan(m1)).isFalse();
    }

    /**
     * Vérifie que la création d'un {@link Money} avec une valeur nulle ou non numérique
     * lève les exceptions appropriées.
     */
    @Test
    @DisplayName("rejette les valeurs nulles ou non numériques")
    void of_rejects_null_and_non_numeric() {
        assertThatThrownBy(() -> Money.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Cannot invoke \"String.toCharArray()\" because \"val\" is null");

        assertThatThrownBy(() -> Money.of("abc"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Vérifie que l'égalité et le hashCode de {@link Money} reposent uniquement sur la valeur
     * numérique du montant, indépendamment de l'échelle (nombre de décimales initial).
     */
    @Test
    @DisplayName("égalité et hashCode reposent sur la valeur numérique")
    void equality_and_hashcode_on_amount() {
        assertThat(Money.of("1.0")).isEqualTo(Money.of("1.00"));
        assertThat(Money.of("1.00").hashCode()).isEqualTo(Money.of("1.000").hashCode());
    }

    @Test
    @DisplayName("Somme de deux objets Money")
    void add_method_works() {
        Money m1 = Money.of("1.05");
        Money m2 = Money.of("0.45");
        Money m3 = Money.of("0.452");
        Money m4 = Money.of("-0.05");

        assertThat(m1.add(m2)).isEqualTo(Money.of("1.50"));
        assertThat(m1.add(m3)).isEqualTo(Money.of("1.50"));
        assertThat(m1.add(m4)).isEqualTo(Money.of("1"));
    }

    @Test
    @DisplayName("Soustraction de deux objets Money")
    void subtract_method_works() {
        Money m1 = Money.of("1.05");
        Money m2 = Money.of("0.45");
        Money m3 = Money.of("0.452");
        Money m4 = Money.of("-0.05");

        assertThat(m1.subtract(m2)).isEqualTo(Money.of("0.60"));
        assertThat(m1.subtract(m3)).isEqualTo(Money.of("0.60"));
        assertThat(m1.subtract(m4)).isEqualTo(Money.of("1.1"));
    }



}
