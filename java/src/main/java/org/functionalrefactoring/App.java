package org.functionalrefactoring;

import org.functionalrefactoring.models.Amount;
import org.functionalrefactoring.models.Cart;
import org.functionalrefactoring.models.CartId;
import org.functionalrefactoring.models.CustomerId;
import org.functionalrefactoring.models.DiscountRule;
import org.functionalrefactoring.models.Storage;

import java.math.BigDecimal;
import java.util.Optional;

public class App {

    static class Saver<T> { // Writer[Optional[T]]

        private final Optional<T> o;

        public Saver(Optional<T> o) {
            this.o = o;
        }

        public void save(Storage<T> storage) {
            o.ifPresent(storage::flush);
        }

    }

    public static void applyAndStoreDiscount(CartId cartId, Storage<Cart> storage) { // TODO void?
        Saver<Cart> saver = appplyDiscountForSave(cartId);
        saver.save(storage); // TODO side effect
    }

    public static Saver<Cart> appplyDiscountForSave(CartId cartId) { // pure
        Optional<Cart> o = applyDiscount(cartId);
        return new Saver<>(o); // not testable
    }

    public static Optional<Cart> applyDiscount(CartId cartId) { // pure
        Cart cart = loadCart(cartId);

        if (cart != Cart.MissingCart) {
            DiscountRule rule = lookupDiscountRule(cart.customerId);

            if (rule != DiscountRule.NoDiscount) {
                Amount discount = rule.apply(cart);
                Cart updatedCart = updateAmount(cart, discount);
                return Optional.of(updatedCart);
            }
        }

        return Optional.empty();
    }

    // ideas
    // * make pure - return a saver function (Optional[Writer[Cart]])
    // * make SRP - split cart creation from save, only save needs storage

    private static Cart loadCart(CartId id) { // Cart is immutable, function is pure
        if (id.value.contains("gold"))
            return new Cart(id, new CustomerId("gold-customer"), new Amount(new BigDecimal(100)));
        if (id.value.contains("normal"))
            return new Cart(id, new CustomerId("normal-customer"), new Amount(new BigDecimal(100)));
        return Cart.MissingCart;
    }

    private static DiscountRule lookupDiscountRule(CustomerId id) { // DiscountRule is function, function is pure
        if (id.value.contains("gold")) return new DiscountRule(App::half);
        return DiscountRule.NoDiscount;
    }

    private static Cart updateAmount(Cart cart, Amount discount) { // Cart is immutable, function is pure
        return new Cart(cart.id, cart.customerId, new Amount(cart.amount.value.subtract(discount.value)));
    }

    private static void save(Cart cart, Storage<Cart> storage) { // TODO side effect
        storage.flush(cart);
    }

    private static Amount half(Cart cart) {
        return new Amount(cart.amount.value.divide(new BigDecimal(2)));
    }
}
