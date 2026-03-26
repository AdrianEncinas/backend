package com.assetstrack.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.assetstrack.backend.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long>{

}
