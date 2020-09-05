package com.gracenote.content.auth.domain.horizontal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gracenote.content.auth.domain.vertical.VerticalService;
import com.gracenote.content.auth.persistence.entity.Horizontal;
import com.gracenote.content.auth.persistence.entity.HorizontalApplication;
import com.gracenote.content.auth.persistence.entity.Vertical;

@Service
public class HorizontalService {

	@Autowired
	HorizontalRepository horizontalRepository;
	
	@Autowired
	VerticalService verticalService;
	
	/**
     * Get all application data
     *
     * @return list restricted and unrestricted applications from database
     */
    public List<Horizontal> getAllHorizontals() {
    	List<Horizontal> horizontals = horizontalRepository.findAllByOrderByHorizontalName();        
        return horizontals;
    }
    
    public List<String> getAllHorizontalName() {
    	List<String> horizontalList=new ArrayList<>();
    	List<Horizontal> horizontals = horizontalRepository.findAllByOrderByHorizontalName();    
    	for (Horizontal horizontal : horizontals) {
    		horizontalList.add(horizontal.getHorizontalName());
		}
        return horizontalList;
    }
    
    public List<HorizontalApplication> getHorizontalByName(List<String> horizontalName,int applicationId){
    	List<Horizontal> horizontalList=horizontalRepository.findAllByOrderByHorizontalName(horizontalName);
    	 for (Horizontal horizontal : horizontalList){
    		 getHorizontalApplication(applicationId,horizontal.getId());
		 }
    	 return horizontalRepository.findAllByOrderByHorizontalName(horizontalName,applicationId);
    }
    
    public Horizontal getHorizontalByName(String horizontalName){
    	return horizontalRepository.findAllByOrderByHorizontalName(horizontalName);
    }
    
    public List<HorizontalApplication> getHorizontalApplication(int applicationId){
    	return horizontalRepository.findApplicationHorizontal(applicationId);
    }
    
    public List<HorizontalApplication> getHorizontalApplication(int applicationId,int horizontalId){
    	List<HorizontalApplication> horizontalApplicationList=horizontalRepository.findApplicationHorizontal(applicationId,horizontalId);
    	if(horizontalApplicationList.isEmpty()) {
    		HorizontalApplication horizontalApplication=new HorizontalApplication();
    		horizontalApplication.setApplicationId(applicationId);
    		horizontalApplication.setHorizontalId(horizontalId);
    		horizontalApplicationList=save(horizontalApplication);
    	}
    	return horizontalApplicationList;
    }
    
    public List<HorizontalApplication> save(HorizontalApplication horizontalApplication) {
    	return horizontalRepository.save(horizontalApplication);
    }
    
    public List<Map<String,Object>> saveHorizontal(String horizontalName){
    	Horizontal horizontal=new Horizontal();
    	horizontal.setHorizontalName(horizontalName);
    	horizontalRepository.save(horizontal);
    	return verticalService.getVerticalData();
    }
    
}
