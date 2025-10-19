package com.womtech.service.impl;

import com.womtech.service.BaseService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public abstract class BaseServiceImpl<T, ID> implements BaseService<T, ID> {

	protected final JpaRepository<T, ID> repo;

	protected BaseServiceImpl(JpaRepository<T, ID> repo) {
		this.repo = repo;
	}

	@Override
	public T save(T entity) {
		return repo.save(entity);
	}

	@Override
	public Optional<T> findById(ID id) {
		return repo.findById(id);
	}

	@Override
	public List<T> findAll() {
		return repo.findAll();
	}

	@Override
	public void deleteById(ID id) {
		repo.deleteById(id);
	}

	@Override
	public boolean existsById(ID id) {
		return repo.existsById(id);
	}

	@Override
	public long count() {
		return repo.count();
	}
}
