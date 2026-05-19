package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessorMockTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private OrderProcessor orderProcessor;

    private ShoppingCart cart;
    private Product widget;
    private Product gadget;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        widget = new Product("P001", "Widget", 25.0, 100);
        gadget = new Product("P002", "Gadget", 50.0, 50);
    }

    // 1:Happy path 
    // Inventory is available and payment is successful.
    @Test
    void process_inventoryOkAndPaymentOk_returnsOrder() {
        cart.addItem(widget, 2); // Total: 50.0

        // Mocking the behavior
        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(paymentGateway.charge("customer-1", 50.0)).thenReturn(true);

        Order order = orderProcessor.process("customer-1", cart);

        // Verification
        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("customer-1");
        assertThat(order.getTotal()).isEqualTo(50.0);
        verify(paymentGateway).charge("customer-1", 50.0);
    }

    // 2: Inventory failure 
    // Item is out of stock. Order must be null and payment should never be called.
    @Test
    void process_inventoryFails_returnsNullAndDoesNotCharge() {
        cart.addItem(widget, 2);

        // Mocking inventory to fail
        when(inventoryService.isAvailable(widget, 2)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        // Verification
        assertThat(order).isNull();
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    // 3: Payment failure 
    // Inventory is fine, but credit card is declined. Order must be null.
    @Test
    void process_paymentFails_returnsNull() {
        cart.addItem(widget, 2); // Total: 50.0

        // Mocking inventory to pass, but payment to fail.
        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(paymentGateway.charge("customer-1", 50.0)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNull();
    }

    //4: Partial quantity 
    // One item is in stock, the other is not. The entire order should be cancelled.
    @Test
    void process_partialInventory_returnsNullAndDoesNotCharge() {
        cart.addItem(widget, 1);
        cart.addItem(gadget, 1);

        // Widget has stock, Gadget does not. 
        // Using lenient() to prevent strict stubbing errors if the code fails fast.
        lenient().when(inventoryService.isAvailable(widget, 1)).thenReturn(true);
        lenient().when(inventoryService.isAvailable(gadget, 1)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        // Verification
        assertThat(order).isNull();
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }
}