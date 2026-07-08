package com.njbank.ticket.repository;

import com.njbank.ticket.entity.CustomerAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerAssigneeRepository extends JpaRepository<CustomerAssignee, Long> {
    Optional<CustomerAssignee> findByCustomerNo(String customerNo);
}