package com.gracenote.content.auth.domain.vertical;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gracenote.content.auth.persistence.entity.Vertical;

public interface VerticalRepository extends JpaRepository<Vertical, Integer> {
	List<Vertical> findAllByOrderByVerticalName();
	
	@Query("SELECT v FROM Vertical v inner join v.horizontalApplication aa where v.verticalName NOT IN :verticalName and aa.applicationId = :applicationId")
	List<Vertical> findByNotInVerticalName(@Param("verticalName")List<String> verticalName,@Param("applicationId")Integer applicationId);
	
	@Query("SELECT v FROM Vertical v inner join v.horizontalApplication aa where v.verticalName = :verticalName and aa.id = :id")
	Vertical findByVerticalName(@Param("verticalName")String verticalName,@Param("id") int id);
	
	@Query("SELECT u FROM Vertical u join fetch u.horizontalApplication aa WHERE u.verticalName = :verticalName")
	Vertical findByVerticalName(@Param("verticalName")String verticalName);
	
	@Query("SELECT u FROM Vertical u WHERE u.verticalName = :verticalName")
	Vertical findAllByVerticalName(@Param("verticalName")String verticalName);
	
	@Query("SELECT u FROM Vertical u join fetch u.horizontalApplication aa WHERE aa.id IN :id")
    List<Vertical> findByApplicationHorizontalApplicationIdIn(@Param("id") int id);
	
	@Query("SELECT u FROM Vertical u join fetch u.horizontalApplication aa WHERE aa.id = :id and aa.id = :verticalId")
    List<Vertical> findByApplicationHorizontalApplicationIdIn(@Param("id") int id,@Param("verticalId") int verticalId);
	
	@Query("SELECT u FROM Vertical u join fetch u.horizontalApplication aa WHERE aa.id IN :id and u.id = :verticalId")
    List<Vertical> findByHorizontalApplicationIdInVertical(@Param("id") List<Integer> id,@Param("verticalId") int verticalId);
	
	
	Vertical findAllByOrderById(@Param("id") int id);
	
}
