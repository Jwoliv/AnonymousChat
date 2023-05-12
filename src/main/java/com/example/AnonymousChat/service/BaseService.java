package com.example.AnonymousChat.service;

import com.example.AnonymousChat.model.BaseEntity;
import com.example.AnonymousChat.repository.BaseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public abstract class BaseService<E extends BaseEntity, R extends BaseRepository<E>> {
    protected final R repository;

    public BaseService(R repository) {
        this.repository = repository;
    }

    @Transactional
    public void save(E entity) {
        repository.save(entity);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<E> findAll() {
        return repository.findAll();
    }

    public Optional<E> findById(Long id) {
        return repository.findById(id);
    }

}