package com.gracenote.content.auth.domain.application;

import com.gracenote.content.auth.persistence.entity.Application;
import com.gracenote.content.auth.persistence.entity.ApplicationAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Integer> {

    @Query("SELECT aa FROM ApplicationAuthority aa WHERE aa.application_id = :application_id AND aa.isDefaultRole = true AND aa.isDeleted = false")
    ApplicationAuthority findDefaultApplicationAutority(@Param("application_id") int application_id);

    Application findApplicationByApplicationName(String applicationName);
    
    Application findApplicationByClientId(String clientId);

    List<Application> findAllByIsDeletedFalseOrderByApplicationName();
    
    List<Application> findAllByOrderByApplicationName();

    Application findApplicationByApplicationId(int applicationId);

    @Query("SELECT a FROM Application a join fetch a.applicationAuthority aa WHERE aa.applicationAuthorityId IN :idSet and (aa.role_name ='ROLE_ADMIN' OR aa.role_name = 'ROLE_SSO_ADMIN')")
    List<Application> getAdminUserApplication(@Param("idSet") List<Integer> idSet);

    @Query("SELECT a FROM Application a join fetch a.applicationAuthority aa WHERE aa.applicationAuthorityId IN :idList and a.isDeleted =false ORDER BY a.applicationName")
    List<Application> getExternalUserApplication(@Param("idList") List<Integer> list);

    @Query("SELECT DISTINCT a FROM Application a join fetch a.applicationAuthority aa WHERE (aa.applicationAuthorityId IN :idList or a.isRestricted =false) and a.isDeleted =false ORDER BY a.applicationName")
    List<Application> getInternalUserApplication(@Param("idList") List<Integer> ids);
}
