package com.gym.crm.app.repository;

import com.gym.crm.app.domain.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeRepository {

    List<Trainee> findAll();

    Trainee saveTrainee(Trainee trainee);

    Optional<Trainee> findByUsername(String username);

    void deleteByUserName(String userName);
}
