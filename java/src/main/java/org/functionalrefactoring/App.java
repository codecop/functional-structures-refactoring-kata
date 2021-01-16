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

        private final Optional<T> item;

        public Saver(Optional<T> item) {
            this.item = item;
        }

        public void saveTo(Storage<T> storage) {
            item.ifPresent(storage::flush);
        }

    }

    public static void applyAndStoreDiscount(CartId cartId, Storage<Cart> storage) { // TODO void
        Saver<Cart> saver = appplyDiscountForSave(cartId);
        saver.saveTo(storage); // TODO side effect
    }

    public static Saver<Cart> appplyDiscountForSave(CartId cartId) { // pure
        Optional<Cart> o = applyDiscount(cartId);
        return new Saver<>(o); // not testable
    }

    public static Optional<Cart> applyDiscount(CartId cartId) { // pure
        Optional<Cart> oCart = loadCart(cartId);
        return oCart.flatMap(cart -> {
            Optional<DiscountRule> oRule = lookupDiscountRule(cart.customerId);
            return oRule.map(rule -> {
                Amount discount = rule.apply(cart);
                Cart updatedCart = updateAmount(cart, discount);
                return updatedCart;
            });
        });
    }

    // ideas
    // * make pure - return a saver function (Optional[Writer[Cart]])
    // * make SRP - split cart creation from "save", only "save" needs storage

    private static Optional<Cart> loadCart(CartId id) { // Cart is immutable, function is pure
        if (id.value.contains("gold"))
            return Optional.of(new Cart(id, new CustomerId("gold-customer"), new Amount(new BigDecimal(100))));
        if (id.value.contains("normal"))
            return Optional.of(new Cart(id, new CustomerId("normal-customer"), new Amount(new BigDecimal(100))));
        return Optional.empty();
    }

    private static Optional<DiscountRule> lookupDiscountRule(CustomerId id) { // DiscountRule is function, function is pure
        if (id.value.contains("gold")) return Optional.of(new DiscountRule(App::half));
        return Optional.empty();
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
