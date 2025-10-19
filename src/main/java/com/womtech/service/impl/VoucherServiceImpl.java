package com.womtech.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.womtech.entity.Voucher;
import com.womtech.repository.VoucherRepository;
import com.womtech.service.VoucherService;

@Service
@Transactional
public class VoucherServiceImpl extends BaseServiceImpl<Voucher, String> implements VoucherService {

    private final VoucherRepository voucherRepository;

    public VoucherServiceImpl(JpaRepository<Voucher, String> repo, VoucherRepository voucherRepository) {
        super(repo);
        this.voucherRepository = voucherRepository;
    }

    @Override
    public Voucher create(Voucher voucher) {
        if (voucher.getCode() == null || voucher.getCode().isBlank()) {
            throw new IllegalArgumentException("Mã voucher không được để trống");
        }

        if (voucherRepository.findByCode(voucher.getCode()).isPresent()) {
            throw new IllegalStateException("Mã voucher đã tồn tại");
        }

        if (voucher.getStatus() == null) {
            voucher.setStatus(1);
        }

        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher update(Voucher voucher) {
        Voucher existing = voucherRepository.findById(voucher.getVoucherID())
                .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại"));

        existing.setCode(voucher.getCode());
        existing.setDiscount(voucher.getDiscount());
        existing.setMin_price(voucher.getMin_price());
        existing.setExpire_date(voucher.getExpire_date());
        existing.setOwner(voucher.getOwner());
        existing.setStatus(voucher.getStatus());

        return voucherRepository.save(existing);
    }

    @Override
    public void delete(String voucherId) {
        if (!voucherRepository.existsById(voucherId)) {
            throw new IllegalArgumentException("Voucher không tồn tại");
        }
        voucherRepository.deleteById(voucherId);
    }

    @Override
    public Optional<Voucher> findByCode(String code) {
        return voucherRepository.findByCode(code);
    }

    @Override
    public Page<Voucher> search(String code, Integer status, String ownerId, Pageable pageable) {
        return voucherRepository.searchVouchers(code, status, ownerId, pageable);
    }

    @Override
    public List<Voucher> getAllActive() {
        return voucherRepository.findByStatus(1);
    }

    @Override
    public void enableVoucher(String voucherId) {
        voucherRepository.findById(voucherId).ifPresent(v -> {
            v.setStatus(1);
            voucherRepository.save(v);
        });
    }

    @Override
    public void disableVoucher(String voucherId) {
        voucherRepository.findById(voucherId).ifPresent(v -> {
            v.setStatus(0);
            voucherRepository.save(v);
        });
    }
}