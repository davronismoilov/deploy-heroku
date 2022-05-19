package uz.oliymahad.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.oliymahad.userservice.model.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Boolean existsByPhoneNumber(String phoneNumber);
    @Query("select u from UserEntity u where u.email = ?1")
    Optional<UserEntity> findByEmail(String email);
}
