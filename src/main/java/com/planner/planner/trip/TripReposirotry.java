package com.planner.planner.trip;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TripReposirotry extends JpaRepository<Trip, UUID> {
}
