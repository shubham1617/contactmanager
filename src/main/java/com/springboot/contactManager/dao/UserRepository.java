package com.springboot.contactManager.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.springboot.contactManager.entity.User;

public interface UserRepository extends  JpaRepository<User,Integer>{

	@Query("Select u from User u where u.email= :email")
	public User getUserbyUserName(@Param("email") String email);
}
