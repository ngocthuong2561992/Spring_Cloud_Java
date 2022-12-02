package com.javatechie.os.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.os.api.common.InventoryResponse;
import com.javatechie.os.api.common.OrderLineItemsDto;
import com.javatechie.os.api.common.OrderRequest;
import com.javatechie.os.api.common.Payment;
import com.javatechie.os.api.common.TransactionRequest;
import com.javatechie.os.api.common.TransactionResponse;
import com.javatechie.os.api.entity.Order;
import com.javatechie.os.api.entity.OrderLineItems;
import com.javatechie.os.api.repository.OrderRepository;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RefreshScope
@Slf4j
public class OrderService {

	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	@Lazy
	private RestTemplate restTemplate;
	private final WebClient webClient = null;

//    @Value("${microservice.payment-service.endpoints.endpoint.uri}")
	private String ENDPOINT_URL = "http://localhost:9191/payment/doPayment";

	public TransactionResponse saveOrder(TransactionRequest request) throws JsonProcessingException {
		String response = "";
		Order order = request.getOrder();
		Payment payment = request.getPayment();
		payment.setOrderId(order.getId());
		payment.setAmount(order.getPrice());
		// rest call
		log.info("Order-Service Request : " + new ObjectMapper().writeValueAsString(request));
		Payment paymentResponse = restTemplate.postForObject(ENDPOINT_URL, payment, Payment.class);

		response = paymentResponse.getPaymentStatus().equals("success")
				? "payment processing successful and order placed"
				: "there is a failure in payment api , order added to cart";
		log.info("Order Service getting Response from Payment-Service : "
				+ new ObjectMapper().writeValueAsString(response));
		orderRepository.save(order);
		return new TransactionResponse(order, paymentResponse.getAmount(), paymentResponse.getTransactionId(),
				response);
	}

	public List<Order> findAll() {
		return orderRepository.findAll();
	}

	public Order findById(Integer id) {
		return orderRepository.findById(id).get();
	}

	public void save(Order req) {
		try {
			boolean exist = orderRepository.existsById(req.getId());
			if (exist) {
				throw new DuplicateKeyException("exist data");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(Order req) {
		try {
			Boolean exist = orderRepository.existsById(req.getId());
			if (exist) {
				orderRepository.save(req);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delete(Integer id) {
		try {
			orderRepository.deleteById(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Boolean existOrder(Integer id) {
		return orderRepository.existsById(id);
	}

	public void placeOrder(OrderRequest orderRequest) {
		try {
			Order order = new Order();
			order.setOrderNumber(UUID.randomUUID().toString());
			List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream().map(this::mapToDto)
					.toList();

			order.setOrderLineItemsList(orderLineItems);

			List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();
			// call inventory service, and place order ì product is in stock
			InventoryResponse[] inventoryResponseArr = webClient.get()
					.uri("http://localhost:8082/order/inventory",
							uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
					.retrieve().bodyToMono(InventoryResponse[].class).block();
			Boolean result = Arrays.stream(inventoryResponseArr).allMatch(InventoryResponse::isInStock);
			if (result) {
				orderRepository.save(order);
			} else {
				throw new IllegalArgumentException("product is not in stock, please try again later");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
		OrderLineItems orderLineItems = new OrderLineItems();
		orderLineItems.setPrice(orderLineItemsDto.getPrice());
		orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
		orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
		return orderLineItems;
	}
}
