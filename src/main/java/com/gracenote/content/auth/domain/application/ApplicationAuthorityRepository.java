package com.gracenote.content.auth.domain.application;

import com.gracenote.content.auth.persistence.entity.ApplicationAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * @author deepak on 18/9/17.
 */
public interface ApplicationAuthorityRepository extends JpaRepository<ApplicationAuthority, Integer> {


    @Query("SELECT aa FROM ApplicationAuthority aa WHERE aa.application_id = :application_id AND aa.isDeleted = false")
    Set<ApplicationAuthority> getApplicationsAuthorities(@Param("application_id") int application_id);

    @Query("SELECT aa FROM ApplicationAuthority aa WHERE aa.application_id = :application_id AND aa.isDefaultRole = true AND aa.isDeleted = false")
    ApplicationAuthority getApplicationAuthorityIsDefaultTrue(@Param("application_id") int application_id);
}
