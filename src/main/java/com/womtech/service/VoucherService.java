package com.womtech.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.womtech.entity.Voucher;

public interface VoucherService extends BaseService<Voucher, String> {
    Voucher create(Voucher voucher);
    Voucher update(Voucher voucher);
    void delete(String voucherId);
    Optional<Voucher> findById(String voucherId);
    Optional<Voucher> findByCode(String code);
    Page<Voucher> search(String code, Integer status, String ownerId, Pageable pageable);
    List<Voucher> getAllActive();
    void enableVoucher(String voucherId);
    void disableVoucher(String voucherId);
}
