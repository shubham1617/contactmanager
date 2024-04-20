package com.springboot.contactManager.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.springboot.contactManager.entity.ContactDetails;

public interface ContactRepository extends JpaRepository<ContactDetails, Integer> {

    @Query("from ContactDetails as c where c.user.uid=:userId")
    // currentPage -- total no of contacts
    // Contact Per Page --5
    public Page<ContactDetails> findContactByUser(@Param("userId") int userId, Pageable p);
}
