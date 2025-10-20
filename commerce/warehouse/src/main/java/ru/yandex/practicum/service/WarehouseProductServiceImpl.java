package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.cart.ShoppingCartDto;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.model.Booking;
import ru.yandex.practicum.model.Dimension;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.BookingRepository;
import ru.yandex.practicum.repository.WarehouseProductRepository;
import ru.yandex.practicum.warehouse.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseProductServiceImpl implements WarehouseProductService {
    private static final String[] ADDRESSES = new String[] {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];
    private final WarehouseProductRepository warehouseRepository;
    private final BookingRepository bookingRepository;
    private final WarehouseProductMapper mapper;

    @Override
    @Transactional
    public void addProductInWarehouse(NewProductInWarehouseRequest request) {
        if (warehouseRepository.findById(request.getProductId()).isPresent()) {
            throw new SpecifiedProductAlreadyInWarehouseException
                    ("Товар с id: " + request.getProductId() + " уже зарегистрирован на складе");
        }
        WarehouseProduct product = warehouseRepository.save(mapper.toWarehouseProduct(request));
        log.info("Создан product: {}", product);
    }

    @Override
    @Transactional
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        Map<UUID, Long> cartProducts = shoppingCartDto.getProducts();
        Map<UUID, WarehouseProduct> products = getProductsOrThrow(cartProducts.keySet());
        BigDecimal weight = BigDecimal.ZERO;
        BigDecimal volume = BigDecimal.ZERO;
        boolean fragile = false;
        for (Map.Entry<UUID, Long> cartProduct : cartProducts.entrySet()) {
            WarehouseProduct product = products.get(cartProduct.getKey());
            if (cartProduct.getValue() > product.getQuantity()) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Ошибка, товар из корзины не находится в требуемом количестве на складе: " + cartProduct.getValue());
            }
            Dimension dimension = product.getDimension();
            BigDecimal quantity = BigDecimal.valueOf(cartProduct.getValue());
            weight = weight.add(dimension.getWidth().multiply(quantity));
            volume = volume.add(dimension.getHeight()
                    .multiply(product.getWeight())
                    .multiply(dimension.getDepth())
                    .multiply(quantity));
            fragile = fragile || product.isFragile();
        }
        return new BookedProductsDto(weight, volume, fragile);
    }

    @Override
    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseProduct product = warehouseRepository.findById(request.getProductId()).orElseThrow(() -> {
            log.error("Нет информации о товаре на складе c id: {}", request.getProductId());
            return new NoSpecifiedProductInWarehouseException("Нет информации о товаре на складе id: " + request.getProductId());
        });
        product.setQuantity(product.getQuantity() + request.getQuantity());
        WarehouseProduct updatedProduct = warehouseRepository.save(product);
        log.info("Обновление количества product: {}", updatedProduct);
    }

    @Override
    public AddressDto getAddressWarehouse() {
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }

    @Override
    public void shippedToDelivery(ShipperToDeliveryRequest request) {
        UUID orderId = request.getOrderId();
        UUID deliveryId = request.getDeliveryId();

        Booking booking = bookingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Не найдена бронь для orderId: " + orderId));

        booking.setDeliveryId(deliveryId);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void returnProducts(Map<UUID, Long> products) {
        Map<UUID, WarehouseProduct> productsInWarehouse = getProductsOrThrow(products.keySet());
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantityToReturn = entry.getValue();

            WarehouseProduct product = productsInWarehouse.get(productId);
            if (product == null) {
                throw new NoSpecifiedProductInWarehouseException("Нет информации о товаре на складе id: " + productId);
            }

            product.setQuantity(product.getQuantity() + quantityToReturn);
        }
        warehouseRepository.saveAll(productsInWarehouse.values());
    }

    @Override
    @Transactional
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        UUID orderId = request.getOrderId();
        Map<UUID, Long> requestedProducts = request.getProducts();

        Map<UUID, WarehouseProduct> warehouseProducts = getProductsOrThrow(requestedProducts.keySet());

        for (Map.Entry<UUID, Long> entry : requestedProducts.entrySet()) {
            WarehouseProduct product = warehouseProducts.get(entry.getKey());
            if (product == null || product.getQuantity() < entry.getValue()) {
                throw new ProductInShoppingCartLowQuantityInWarehouse("Недостаточно товара ID: " + entry.getKey());
            }
            product.setQuantity(product.getQuantity() - entry.getValue());
        }
        warehouseRepository.saveAll(warehouseProducts.values());

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : requestedProducts.entrySet()) {
            WarehouseProduct product = warehouseProducts.get(entry.getKey());
            BigDecimal q = BigDecimal.valueOf(entry.getValue());
            Dimension d = product.getDimension();

            totalWeight = totalWeight.add(product.getWeight().multiply(q));
            totalVolume = totalVolume.add(
                    d.getWidth().multiply(d.getHeight()).multiply(d.getDepth()).multiply(q)
            );
            fragile = fragile || product.isFragile();
        }

        Booking booking = Booking.builder()
                .orderId(orderId)
                .products(requestedProducts)
                .build();
        bookingRepository.save(booking);

        log.info("Создана бронь заказа {}: {}", orderId, booking.getBookingId());

        return new BookedProductsDto(totalWeight, totalVolume, fragile);
    }

    private Map<UUID, WarehouseProduct> getProductsOrThrow(Set<UUID> productIds) {
        Map<UUID, WarehouseProduct> products = warehouseRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        if (products.size() != productIds.size()) {
            throw new NoSpecifiedProductInWarehouseException("Некоторые товары не найдены на складе");
        }
        return products;
    }
}