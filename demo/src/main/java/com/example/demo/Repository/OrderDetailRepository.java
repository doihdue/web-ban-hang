package com.example.demo.Repository;

import com.example.demo.Model.OrderDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("SELECT od.productName, SUM(od.quantity) as totalSold FROM OrderDetail od GROUP BY od.productName ORDER BY totalSold DESC")
    List<Object[]> findTopProductSales(Pageable pageable);
}