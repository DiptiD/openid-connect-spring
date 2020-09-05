package com.gracenote.content.auth.domain.horizontal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gracenote.content.auth.persistence.entity.Horizontal;
import com.gracenote.content.auth.persistence.entity.HorizontalApplication;

public interface HorizontalRepository extends JpaRepository<Horizontal, Integer> {
	
	List<Horizontal> findAllByOrderByHorizontalName();
	
	@Query("SELECT aa FROM HorizontalApplication aa join fetch aa.horizontal h WHERE h.horizontalName IN :horizontalName and aa.applicationId = :applicationId")
	List<HorizontalApplication> findAllByOrderByHorizontalName(@Param("horizontalName") List<String> horizontalName,@Param("applicationId") int applicationId);
	
	@Query("SELECT h FROM Horizontal h WHERE h.horizontalName = :horizontalName")
	Horizontal findAllByOrderByHorizontalName(@Param("horizontalName") String horizontalName);
	
	@Query("SELECT h FROM Horizontal h WHERE h.horizontalName IN :horizontalName")
	List<Horizontal> findAllByOrderByHorizontalName(@Param("horizontalName") List<String> horizontalName);
	
	@Query("SELECT aa FROM HorizontalApplication aa WHERE aa.applicationId = :applicationId")
	List<HorizontalApplication> findApplicationHorizontal(@Param("applicationId") int applicationId);
	
	@Query("SELECT aa FROM HorizontalApplication aa WHERE aa.applicationId = :applicationId and aa.horizontalId = :horizontalId")
	List<HorizontalApplication> findApplicationHorizontal(@Param("applicationId") int applicationId,@Param("horizontalId") int horizontalId);
	
	List<HorizontalApplication> save(HorizontalApplication horizontalApplication);
	
}
