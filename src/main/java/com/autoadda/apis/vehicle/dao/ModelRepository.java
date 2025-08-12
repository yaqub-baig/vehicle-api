/**
 * 
 */
package com.autoadda.apis.vehicle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.vehicle.entity.Model;

@Repository
public interface ModelRepository extends JpaRepository<Model, Integer> {
}
