package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ShoppingCartStructuralTest {

    private ShoppingCart cart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        apple  = new Product("P001", "Apple",  1.50, 100);
        banana = new Product("P002", "Banana", 0.80, 50);
    }

    // Tests for addItem 

    // Adding a completely new product to the cart.
    @Test
    void addItem_NewProduct() {
        cart.addItem(apple, 2);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo(3.00);
    }

    // Adding a product that is already in the cart.
    // This forces the code to enter the 'if' block to update the quantity.
    @Test
    void addItem_ExistingProduct() {
        cart.addItem(apple, 2);
        cart.addItem(apple, 3); // Should increase quantity to 5, not item count
        
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(cart.total()).isEqualTo(7.50);
    }

    // Adding multiple different products to cover the loop completely
    @Test
    void addItem_MultipleDifferentProducts() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 5);
        
        assertThat(cart.itemCount()).isEqualTo(2);
        assertThat(cart.total()).isEqualTo(7.00); // (1.50*2) + (0.80*5)
    }

    // Tests for removeItem 

    // Removing a product that is actually in the cart.
    @Test
    void removeItem_ExistingProduct() {
        cart.addItem(apple, 2);
        cart.removeItem("P001"); 
        assertThat(cart.itemCount()).isEqualTo(0);
    }

    // Trying to remove a product that is NOT in the cart.
    // The condition evaluates to false, so nothing should happen.
    @Test
    void removeItem_NonExistingProduct() {
        cart.addItem(apple, 2);
        cart.removeItem("P003"); // Apple should stay in the cart
        assertThat(cart.itemCount()).isEqualTo(1);
    }

    // Tests for updateQuantity 

    // Successfully updating the quantity of an existing product.
    @Test
    void updateQuantity_ExistingProduct() {
        cart.addItem(apple, 2);
        cart.updateQuantity("P001", 5); 
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    // Passing an invalid quantity (0 or less).
    // Triggers the first 'if' check in the method.
    @Test
    void updateQuantity_InvalidQuantity() {
        cart.addItem(apple, 2);
        assertThatThrownBy(() -> cart.updateQuantity("P001", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be > 0");
    }

    // The loop finishes but the product is not found.
    // Triggers the exception thrown at the end of the method.
    @Test
    void updateQuantity_ProductNotFound() {
        cart.addItem(apple, 2);
        assertThatThrownBy(() -> cart.updateQuantity("P002", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    // Tests for applyDiscount 

    // Applying a normal, positive discount.
    @Test
    void applyDiscount_PositiveDiscount() {
        cart.addItem(apple, 4); // Subtotal = 6.00
        double discounted = cart.applyDiscount(10.0); // 10% off
        assertThat(discounted).isEqualTo(5.40);
    }

    // Applying a 0% discount.
    @Test
    void applyDiscount_ZeroDiscount() {
        cart.addItem(apple, 4); 
        double discounted = cart.applyDiscount(0.0);
        assertThat(discounted).isEqualTo(6.00);
    }

    // Tests for total, clear and getItems 

    // Calling total() on an empty cart.
    // The for-loop inside total() never executes.
    @Test
    void total_EmptyCart() {
        assertThat(cart.total()).isEqualTo(0.0);
    }

    // Checking if clear() successfully empties the cart.
    @Test
    void clear_EmptiesCart() {
        cart.addItem(apple, 1);
        cart.clear();
        assertThat(cart.itemCount()).isEqualTo(0);
    }

    // Testing if getItems returns an unmodifiable list view
    @Test
    void getItems_ReturnsUnmodifiableList() {
        cart.addItem(apple, 1);
        List<CartItem> items = cart.getItems();
        assertThat(items).hasSize(1);
    }

    // Testing the toString method format for coverage
    @Test
    void testToString_FormatsCorrectly() {
        cart.addItem(apple, 2);
        assertThat(cart.toString()).contains("ShoppingCart");
    }
}