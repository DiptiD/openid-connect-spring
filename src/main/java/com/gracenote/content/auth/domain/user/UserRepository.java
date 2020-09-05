package com.gracenote.content.auth.domain.user;

import com.gracenote.content.auth.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author deepak on 10/8/17.
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByIsDeletedFalseAndUsernameIgnoreCase(String username);

    List<User> findAllByIsDeletedFalse();

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) and u.isDeleted=false")
    User findByEmail(@Param("email") String email);

    @Query
    User findByEmailAndActivationKey(String email, String activationKey);

    @Query
    User findByEmailAndResetPasswordKey(String email, String resetPasswordKey);

    @Query("SELECT u FROM User u WHERE u.user_id = :user_id and u.isDeleted=false")
    User findByUserId(@Param("user_id") int user_id);

    @Query("SELECT u FROM User u join fetch u.applicationAuthorities aa WHERE aa.applicationAuthorityId IN :authorityIds and u.username is not null and u.isDeleted=false")
    List<User> findByApplicationAuthoritiesApplicationAuthorityIdIn(@Param("authorityIds") List<Integer> applicationAuthorityId);
}
