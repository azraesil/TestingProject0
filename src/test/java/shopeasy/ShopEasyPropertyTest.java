package shopeasy;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import static org.assertj.core.api.Assertions.assertThat;

class ShopEasyPropertyTest {

    private final PriceCalculator calculator = new PriceCalculator();

    // Identity Property 
    // (a) If discount and tax are 0, the price stays the same.
    // (b) Bugs: Math errors where adding 0 changes the total.
    @Property
    void identityProperty(@ForAll("validPrices") double basePrice) {
        double finalPrice = calculator.calculate(basePrice, 0.0, 0.0);
        assertThat(finalPrice).isEqualTo(basePrice);
    }

    // Boundedness Property
    // (a) Final price cannot be negative or higher than base price + max tax.
    // (b) Bugs: Negative price bugs or wrong formula calculations.
    @Property
    void boundednessProperty(
            @ForAll("validPrices") double basePrice,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double discountRate,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double taxRate
    ) {
        double finalPrice = calculator.calculate(basePrice, discountRate, taxRate);
        double maxPossible = basePrice * (1.0 + (100.0 / 100.0));

        assertThat(finalPrice).isGreaterThanOrEqualTo(0.0);
        assertThat(finalPrice).isLessThanOrEqualTo(maxPossible);
    }

    // Cart Commutativity Property
    // (a) Adding item A then B gives the same total as adding B then A.
    // (b) Bugs: Bugs where the order of items changes the final price.
    @Property
    void cartCommutativity(
            @ForAll("randomProducts") Product productA,
            @ForAll("randomProducts") Product productB,
            @ForAll @IntRange(min = 1, max = 10) int qtyA,
            @ForAll @IntRange(min = 1, max = 10) int qtyB
    ) {
        // ÇÖZÜM: jqwik'in iki farklı ürüne kazara aynı ID'yi vermesini engelliyoruz
        Assume.that(!productA.getId().equals(productB.getId()));

        ShoppingCart cart1 = new ShoppingCart();
        cart1.addItem(productA, qtyA);
        cart1.addItem(productB, qtyB);

        ShoppingCart cart2 = new ShoppingCart();
        cart2.addItem(productB, qtyB);
        cart2.addItem(productA, qtyA);

        // ÇÖZÜM: Küsuratlı sayılardaki yuvarlama hatalarına karşı tolerans tanıyoruz
        assertThat(cart1.total()).isCloseTo(cart2.total(), org.assertj.core.data.Offset.offset(0.001));
    }

    // Custom Data Providers 
    
    @Provide
    Arbitrary<Double> validPrices() {
        return Arbitraries.doubles().between(0.0, 10000.0);
    }

    @Provide
    Arbitrary<Product> randomProducts() {
        Arbitrary<String> ids = Arbitraries.strings().alpha().ofLength(5);
        Arbitrary<String> names = Arbitraries.strings().alpha().ofLength(10);
        Arbitrary<Double> prices = Arbitraries.doubles().between(1.0, 500.0);
        Arbitrary<Integer> stocks = Arbitraries.integers().between(10, 100);

        return Combinators.combine(ids, names, prices, stocks).as(Product::new);
    }
}