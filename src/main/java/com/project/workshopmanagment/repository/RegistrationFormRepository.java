package com.project.workshopmanagment.repository;

import com.project.workshopmanagment.entity.RegistrationForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface RegistrationFormRepository extends CrudRepository<RegistrationForm, Long> {
}
