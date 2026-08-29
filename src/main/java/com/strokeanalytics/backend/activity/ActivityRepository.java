package com.strokeanalytics.backend.activity;

import org.springframework.data.jpa.repository.JpaRepository;

/** Standard CRUD access to {@link Activity} rows. */
public interface ActivityRepository extends JpaRepository<Activity, Long> {
}
