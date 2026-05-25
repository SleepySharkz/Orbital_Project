package com.mindmesh.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import com.mindmesh.backend.entity.CourseModule;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {

  List<CourseModule> findByUserIdOrderByCreatedAtDesc(Long userId);

  Optional<CourseModule> findByIdAndUserId(Long id, Long userId);

  // We shall avoid using the @Query, to avoid bugs to be safe
  // This is for a validation check
  boolean existsByUserIdAndCourseCodeIgnoreCaseAndSchoolSemIgnoreCase(
      Long userId,
      String courseCode,
      String schoolSem);
}
