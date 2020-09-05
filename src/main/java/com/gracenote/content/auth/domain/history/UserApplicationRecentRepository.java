package com.gracenote.content.auth.domain.history;

import com.gracenote.content.auth.persistence.entity.Application;
import com.gracenote.content.auth.persistence.entity.UserApplicationRecent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserApplicationRecentRepository extends JpaRepository<UserApplicationRecent, Integer> {

    @Query("SELECT uar FROM UserApplicationRecent uar WHERE uar.user.user_id = :userId ORDER BY login_time DESC")
    List<UserApplicationRecent> getUserApplicationRecent(@Param("userId") int userId);

    @Query("SELECT uar.application FROM UserApplicationRecent uar WHERE uar.user.user_id = :userId AND uar.application.isDeleted = false ORDER BY login_time")
    List<Application> getRecentApplications(@Param("userId") int userId);

}
