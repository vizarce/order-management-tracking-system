package com.ordertracking.orderservice.infrastructure.persistence.adapter;

import com.ordertracking.orderservice.domain.model.Customer;
import com.ordertracking.orderservice.domain.model.valueobject.CustomerId;
import com.ordertracking.orderservice.domain.repository.CustomerRepository;
import com.ordertracking.orderservice.infrastructure.persistence.mapper.CustomerMapper;
import com.ordertracking.orderservice.infrastructure.persistence.repository.CustomerJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CustomerRepositoryAdapter implements CustomerRepository {
    private final CustomerJpaRepository jpaRepository;
    private final CustomerMapper mapper;

    public CustomerRepositoryAdapter(CustomerJpaRepository jpaRepository, CustomerMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Customer save(Customer customer) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(customer)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(CustomerId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(CustomerId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(CustomerId id) {
        return jpaRepository.existsById(id.value());
    }
}
