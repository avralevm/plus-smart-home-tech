package ru.yandex.practicum.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.cart.ShoppingCartDto;
import ru.yandex.practicum.warehouse.AddressDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewOrderRequest {
    @NotNull
    private ShoppingCartDto shoppingCart;
    @NotNull
    private AddressDto deliveryAddress;
}