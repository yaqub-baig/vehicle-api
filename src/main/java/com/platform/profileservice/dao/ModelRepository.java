/**
 * 
 */
package com.platform.profileservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.profileservice.entity.Model;

@Repository
public interface ModelRepository extends JpaRepository<Model, Integer> {
}
