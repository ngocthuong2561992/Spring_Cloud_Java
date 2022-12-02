package com.javatechie.os.api.repository;

import com.javatechie.os.api.common.OrderRequest;
import com.javatechie.os.api.entity.Order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
}
