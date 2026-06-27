package com.mindmesh.backend.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mindmesh.backend.entity.User;

@Repository // THis is the repository layer
public interface UserRepository extends JpaRepository<User, Long> {
  // We get free DB methods signatures thanks to the JpaRepository interface

  // Additional Custom method
  Optional<User> findByEmailIgnoreCase(String email);
  // Since our login verification needs email + passwordhash
  // getReferenceById is usually used when u alrdy know the user exists, and
  // the id, so u just need a reference to that entity
  // Lazily returns the ref, so any sql injection only happens when u start
  // // calling methods on that ref

  // Friend discovery uses a bounded substring search to avoid returning an
  // unbounded portion of the user table.
  List<User> findTop20ByEmailContainingIgnoreCaseOrderByEmailAsc(String email);
}
