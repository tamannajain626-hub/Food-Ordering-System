package com.foodordering.service;

import com.foodordering.dto.OrderItemRequest;
import com.foodordering.dto.OrderItemResponse;
import com.foodordering.dto.OrderRequest;
import com.foodordering.dto.OrderResponse;
import com.foodordering.entity.Customer;
import com.foodordering.entity.MenuItem;
import com.foodordering.entity.Order;
import com.foodordering.entity.OrderItem;
import com.foodordering.exception.ResourceNotFoundException;
import com.foodordering.repository.CustomerRepository;
import com.foodordering.repository.MenuItemRepository;
import com.foodordering.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final MenuItemRepository menuItemRepository;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository, MenuItemRepository menuItemRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No order found with id " + orderId));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("No customer found with id " + request.getCustomerId()));

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(Order.Status.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() < 1) {
                throw new IllegalArgumentException("quantity must be at least 1");
            }

            MenuItem menuItem = menuItemRepository.findById(itemRequest.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("No menu item found with id " + itemRequest.getItemId()));

            if (!menuItem.isAvailable()) {
                throw new IllegalArgumentException(menuItem.getName() + " is currently unavailable");
            }

            BigDecimal priceAtOrdering = menuItem.getPrice();
            BigDecimal lineTotal = priceAtOrdering.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(lineTotal);

            OrderItem orderItem = new OrderItem(menuItem, itemRequest.getQuantity(), priceAtOrdering);
            orderItem.setLineTotal(lineTotal);
            order.addOrderItem(orderItem);
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getOrderItemId(),
                        item.getMenuItem().getItemId(),
                        item.getMenuItem().getName(),
                        item.getQuantity(),
                        item.getPriceAtOrdering(),
                        item.getLineTotal()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getOrderId(),
                order.getCustomer().getCustomerId(),
                order.getCustomer().getName(),
                order.getCustomer().getEmail(),
                itemResponses,
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }
}
