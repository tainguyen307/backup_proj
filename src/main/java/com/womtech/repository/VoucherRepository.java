package com.womtech.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.womtech.entity.Voucher;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByCode(String code);

    @Query("""
    	    SELECT v FROM Voucher v
    	    JOIN v.owner o
    	    WHERE (:code IS NULL OR LOWER(v.code) LIKE LOWER(CONCAT('%', :code, '%')))
    	      AND (:status IS NULL OR v.status = :status)
    	      AND (:ownerId IS NULL OR o.userID = :ownerId)
    	""")
    	Page<Voucher> searchVouchers(@Param("code") String code,
    	                             @Param("status") Integer status,
    	                             @Param("ownerId") String ownerId,
    	                             Pageable pageable);

    List<Voucher> findByStatus(Integer status);
}
