package com.kb.ordering.assignment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface FailedAssignmentEventJpaRepository extends JpaRepository<FailedAssignmentEventEntity, Long> {
}
