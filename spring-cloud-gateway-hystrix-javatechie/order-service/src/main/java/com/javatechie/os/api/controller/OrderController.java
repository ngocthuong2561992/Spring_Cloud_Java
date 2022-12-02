package com.javatechie.os.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.javatechie.os.api.common.OrderRequest;
import com.javatechie.os.api.common.TransactionRequest;
import com.javatechie.os.api.common.TransactionResponse;
import com.javatechie.os.api.entity.Order;
import com.javatechie.os.api.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;


@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@PostMapping("/bookOrder")
	public TransactionResponse bookOrder(@RequestBody TransactionRequest request) throws JsonProcessingException {
		return orderService.saveOrder(request);
	}

	@GetMapping("/find-all")
	private ResponseEntity<?> findAll() {
		return new ResponseEntity<>(orderService.findAll(), HttpStatus.OK);
	}

	@PostMapping("/place-order")
	@ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
	private String placeOrder(@RequestBody OrderRequest orderRequest) {
		orderService.placeOrder(orderRequest);
		return "Order placed Successfully";
	}
	
    public String fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException) {
        return "Oops! Something went wrong, please order after some time!";
    }

	@PostMapping("/createdOrUpdated")
	private ResponseEntity<?> saveUser(@RequestBody Order req) {
		boolean exist = orderService.existOrder(req.getId());
		if (!exist) {
			orderService.save(req);
		} else {
			orderService.update(req);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
