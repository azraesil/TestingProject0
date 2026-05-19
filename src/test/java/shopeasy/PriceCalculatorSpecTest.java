package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class PriceCalculatorSpecTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    //  Partition Analysis 

    // Testing normal, valid inputs.
    // Just checking if the basic math works for typical prices, discounts, and taxes.
    @ParameterizedTest(name = "base={0}, disc={1}%, tax={2}% => expected={3}")
    @CsvSource({
            "100.0, 10.0, 20.0, 108.0",  // typical case: 10% off, then 20% tax
            "50.0,  20.0, 10.0, 44.0",   // 50 - 20% = 40. 40 + 10% tax = 44
            "200.0, 25.0, 8.0,  162.0"   // 200 - 25% = 150. 150 + 8% tax = 162
    })
    void validPartitions(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(0.001));
    }

    //  Boundary Value Analysis 

    // Boundary: What happens if the base price is exactly 0?
    @Test
    void boundary_ZeroBasePrice() {
        assertThat(calculator.calculate(0.0, 10.0, 10.0)).isEqualTo(0.0);
    }

    // Boundary: Testing when there is no discount at all 
    @Test
    void boundary_ZeroDiscount() {
        assertThat(calculator.calculate(100.0, 0.0, 10.0)).isCloseTo(110.0, within(0.001));
    }

    // Boundary: Testing when there is no tax at all 
    @Test
    void boundary_ZeroTax() {
        assertThat(calculator.calculate(100.0, 10.0, 0.0)).isCloseTo(90.0, within(0.001));
    }

    // Boundary: Testing the maximum possible discount (free)
    @Test
    void boundary_MaxDiscount() {
        assertThat(calculator.calculate(100.0, 100.0, 10.0)).isEqualTo(0.0);
    }

    // Boundary: Testing a very high tax rate basically doubles the discounted price
    @Test
    void boundary_MaxTax() {
        assertThat(calculator.calculate(100.0, 10.0, 100.0)).isCloseTo(180.0, within(0.001));
    }

    // Boundary: Hitting both upper limits at the same time (100% discount and 100% tax)
    @Test
    void boundary_MaxDiscountAndMaxTax() {
        assertThat(calculator.calculate(100.0, 100.0, 100.0)).isEqualTo(0.0);
    }

    //  Exceptional / Invalid Inputs 

    // Testing weird/invalid inputs like negative numbers or >100% discount.
    //  We will handle these properly with asserts in Task 3, but for now we just check the raw math.
    @ParameterizedTest(name = "Invalid base={0}, disc={1}%, tax={2}% => result={3}")
    @CsvSource({
            "-100.0, 10.0, 10.0, -99.0",  // Negative base price
            "100.0, -10.0, 10.0, 121.0",  // Negative discount 
            "100.0, 10.0, -10.0, 81.0",   // Negative tax 
            "100.0, 150.0, 10.0, -55.0"   // Discount > 100% 
    })
    void invalidInputs_CalculateMath(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(0.001));
    }
}