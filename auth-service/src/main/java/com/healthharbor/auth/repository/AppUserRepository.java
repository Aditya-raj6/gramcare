package com.healthharbor.auth.repository;

import com.healthharbor.auth.entity.AppUser;
import com.healthharbor.common.crud.CrudRepository;

import java.util.Optional;

public interface AppUserRepository extends CrudRepository<AppUser> {
    Optional<AppUser> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
}
