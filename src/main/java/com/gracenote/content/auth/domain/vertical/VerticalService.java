package com.gracenote.content.auth.domain.vertical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gracenote.content.auth.domain.application.ApplicationService;
import com.gracenote.content.auth.domain.horizontal.HorizontalRepository;
import com.gracenote.content.auth.domain.horizontal.HorizontalService;
import com.gracenote.content.auth.persistence.entity.Application;
import com.gracenote.content.auth.persistence.entity.Horizontal;
import com.gracenote.content.auth.persistence.entity.HorizontalApplication;
import com.gracenote.content.auth.persistence.entity.Vertical;
import com.gracenote.content.auth.util.VerticalPO;

@Service
public class VerticalService {

	@Autowired
	VerticalRepository verticalRepository;
	
	@Autowired
	ApplicationService applicationService;
	
	@Autowired
	HorizontalService horizontalService;
	
	/**
     * Get all vertical data
     *
     * @return list verticals from database
     */
    @SuppressWarnings("unchecked")
	public List<VerticalPO> getAllVerticals() {
        
    	VerticalPO verticalPo=null;
    	List<VerticalPO> verticallist=new ArrayList<VerticalPO>();
    	List<Vertical> verticals = verticalRepository.findAllByOrderByVerticalName();
    	
    	Map<String, List<Application>> applicationMap=applicationService.getAllApplications();

    	for (Vertical vertical : verticals) {
    		Set<Application> allApplicationList=new HashSet<>();
			List<HorizontalApplication> horizontalApplicationlist = vertical.getHorizontalApplication();
			Map<String,Object> map = new HashMap<String,Object>();
			for (HorizontalApplication horizontalApplication : horizontalApplicationlist) {
				List<Application> applicationlist;
				if(!map.isEmpty() && map.get(horizontalApplication.getHorizontal().getHorizontalName())!=null) {
					applicationlist=(List<Application>) map.get(horizontalApplication.getHorizontal().getHorizontalName());
				}
				else {
					applicationlist=new ArrayList<Application>();
				}
				Gson gson=new Gson();
	            Application response=gson.fromJson(horizontalApplication.getApplication().toString(),Application.class);
				for (Application unrestrictapp : applicationMap.get("unrestricted")) {
					if(response!=null && response.getApplicationName().equalsIgnoreCase(unrestrictapp.getApplicationName())) {
						response.setIsRestricted(false);
						break;
					}
					else {
						response.setIsRestricted(true);
					}
				}
				if(!response.getIsDeleted()) {
					applicationlist.add(response);
				}
				if(applicationlist!=null && !applicationlist.isEmpty()) {
					map.put(horizontalApplication.getHorizontal().getHorizontalName(),applicationlist);
				}
				allApplicationList.addAll(applicationlist);
				if(allApplicationList!=null && !allApplicationList.isEmpty()) {
					map.put("All", allApplicationList);
				}
			}
			verticalPo=new VerticalPO();
			verticalPo.setVertical(vertical.getVerticalName());
			if(map!=null && !map.isEmpty()) {
				verticalPo.setHorizontal(map);
				verticallist.add(verticalPo);
			}
		}
        return verticallist;
    }
    
    public List<Map<String,Object>> getVerticalData() {
    	
    	List<String> horizontalData = horizontalService.getAllHorizontalName();
    	List<Vertical> verticalData = verticalRepository.findAllByOrderByVerticalName();
    	Map<String,Object> map=null;
    	List<Map<String,Object>> list=new ArrayList<>();
    	for (Vertical vertical : verticalData) {
    		map=new HashMap<>();
			map.put("vertical",vertical.getVerticalName());
			map.put("horizontal", horizontalData);
			list.add(map);
		}
    	return list;
    }
    
    public void updateVertical(List<String> verticalName,int applicationId) {
    	List<Vertical> verticalData = verticalRepository.findByNotInVerticalName(verticalName,applicationId);
    	Iterator<Vertical> it = verticalData.iterator();
    	while(it.hasNext()) {
			Vertical vertical = it.next();
			vertical.setHorizontalApplication(new ArrayList<>());
			verticalRepository.save(vertical);
		}
    }
    
	public void updateVerticalHorizontalApplication(String verticalhorizontal,int applicationId) {
		List<Map<String,List<String>>> horizontalVerticalMap=new ArrayList<>();
		Gson gson = new Gson();
    	List<String> verticalNameList=new ArrayList<>();
    	
    	horizontalVerticalMap=gson.fromJson(verticalhorizontal,new TypeToken<ArrayList<Map<String,List<String>>>>() {}.getType());
    	for (Map<String,List<String>> map : horizontalVerticalMap) {
    		List<String> verticalName= map.get("verticalName");
        	List<String> horizontalNameList = map.get("horizontalName");
        	if(verticalName!=null && !verticalName.isEmpty() && horizontalNameList!=null && !horizontalNameList.isEmpty()) {
        		verticalNameList.add(verticalName.get(0));
        		List<HorizontalApplication> newhorizontalList = horizontalService.getHorizontalByName(horizontalNameList,applicationId);
        		Vertical vertical=verticalRepository.findAllByVerticalName(verticalName.get(0));
        		List<HorizontalApplication> existinghorizontalApplicationlist=new ArrayList<>();
            	if(!vertical.getHorizontalApplication().isEmpty()) {
            		existinghorizontalApplicationlist =  vertical.getHorizontalApplication();
            		Iterator<HorizontalApplication> existitr = existinghorizontalApplicationlist.listIterator();
            		while(existitr.hasNext()) {
            			HorizontalApplication oldhorizontalApplication = existitr.next();
			    			if(oldhorizontalApplication.getApplicationId()==applicationId) {
			    				existitr.remove();
			    			}
            		}
            		vertical.getHorizontalApplication().addAll(newhorizontalList);
        		}
            	else {
            		vertical = verticalRepository.findAllByVerticalName(verticalName.get(0));
            		vertical.setHorizontalApplication(newhorizontalList);
            	}
            	verticalRepository.save(vertical);
        	}
    	}
    	updateVertical(verticalNameList,applicationId);
    }
    
    public Vertical getVerticalByName(String verticalName,int id) {
    	return verticalRepository.findByVerticalName(verticalName,id);
    }
    
    public Vertical getVerticalByName(String verticalName) {
    	return verticalRepository.findByVerticalName(verticalName);
    }
    
    public List<Vertical> getVertical(int id){
    	return verticalRepository.findByApplicationHorizontalApplicationIdIn(id);
    }
    
    public List<Vertical> getVertical(int id,int verticalId){
    	return verticalRepository.findByApplicationHorizontalApplicationIdIn(id,verticalId);
    }
    
    public List<Map<String,Object>> saveVertical(String verticalName){
    	Vertical oldvertical=verticalRepository.findAllByVerticalName(verticalName);
    	if(oldvertical==null) {
	    	Vertical vertical=new Vertical();
	    	vertical.setVerticalName(verticalName);
	    	verticalRepository.save(vertical);
    	}
    	return getVerticalData();
    }
}
