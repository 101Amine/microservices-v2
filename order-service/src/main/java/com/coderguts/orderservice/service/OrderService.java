package com.coderguts.orderservice.service;


import com.coderguts.orderservice.DTO.OrderLineItemsDTO;
import com.coderguts.orderservice.DTO.OrderRequest;
import com.coderguts.orderservice.model.Order;
import com.coderguts.orderservice.model.OrderLineItems;
import com.coderguts.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());


        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDTOList()
                    .stream()
                    .map(this::mapToDto)
                    .toList();

        order.setOrderLineItemsList(orderLineItems);

        // Call inventory service and place order if product isn't in stock

        Boolean result = webClient.get()
                .uri("http://localhost:8082/api/inventory")
                        .retrieve()
                                .bodyToMono(boolean.class)
                                        .block();

        if (Boolean.TRUE.equals(result)) {
            orderRepository.save(order);
        }
            else{
                throw new IllegalArgumentException("Product not in stock, please try again later");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDTO orderLineItemsDTO) {

        OrderLineItems orderLineItems =  new OrderLineItems();

        orderLineItems.setPrice(orderLineItemsDTO.getPrice());
        orderLineItems.setQuantity(orderLineItemsDTO.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDTO.getSkuCode());

        return orderLineItems;
    }
}
