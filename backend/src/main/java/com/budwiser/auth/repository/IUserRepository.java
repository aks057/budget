package com.budwiser.auth.repository;

import com.budwiser.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByGoogleSubject(String googleSubject);

  boolean existsByEmail(String email);
}
