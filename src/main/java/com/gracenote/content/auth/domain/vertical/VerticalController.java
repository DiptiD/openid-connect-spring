package com.gracenote.content.auth.domain.vertical;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gracenote.content.auth.domain.horizontal.HorizontalService;
import com.gracenote.content.auth.persistence.entity.HorizontalApplication;
import com.gracenote.content.auth.persistence.entity.Vertical;
import com.gracenote.content.auth.util.VerticalPO;


/**
 * Rest end point of Vertical service of content auth.
 */

@RestController
@EnableResourceServer
@RequestMapping(value = "api/v1/verticals")
public class VerticalController {

	private final Logger log = LoggerFactory.getLogger(VerticalController.class);
	
	@Autowired
	VerticalService verticalService;
	
	@Autowired
	HorizontalService horizontalService;
	
	/**
     * Get list of all verticals
     *
     * @return list of verticals.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/", method = GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getVerticalDetails() {
        try {
        	log.debug("Receive request to get Application from vertical");
            List<VerticalPO> verticals = verticalService.getAllVerticals();
            return new ResponseEntity(verticals, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Exception occurred at get all application verticals: " + e.getMessage());
            return new ResponseEntity("{\"Response\":\"No verticals found\"}", HttpStatus.NOT_FOUND);
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "verticalhorizontal/", method = GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllVerticalAndHorizontalData() {
    	log.debug("Receive request to get HorizontalVerticalName ");
    	try {
    		List<Map<String,Object>> verticalhorizontalData=verticalService.getVerticalData();
    		return new ResponseEntity(verticalhorizontalData, HttpStatus.OK);
    	}
    	catch (DataAccessException e) {
            log.error("Exception occurred to get HorizontalVerticalName" + e.getMessage());
            return new ResponseEntity("{\"Response\":\"No data\"}", HttpStatus.NOT_ACCEPTABLE);
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
    @RequestMapping(value = "/vertical/horizontal/{applicationId}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getHorizontalVerticalName(@PathVariable final int applicationId) {
        log.debug("Receive request to get HorizontalVerticalName with id {}", applicationId);
        List<String> verticalhorizontallist=new ArrayList<>();
        try {
        	List<HorizontalApplication> horizontalApplicationlist =  horizontalService.getHorizontalApplication(applicationId);
			for (HorizontalApplication horizontalApplication : horizontalApplicationlist) {
				String horizontalName = horizontalApplication.getHorizontal().getHorizontalName();
				int id = horizontalApplication.getId();
				List<Vertical> verticals =verticalService.getVertical(id);
				for (Vertical vertical : verticals) {					
					verticalhorizontallist.add(vertical.getVerticalName()+"_"+horizontalName); 
				}
			}
            return new ResponseEntity(verticalhorizontallist, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Exception occurred to get HorizontalVerticalName {0}", applicationId + " : " + e.getMessage());
            return new ResponseEntity("{\"Response\":\"Invalid application ID\"}", HttpStatus.NOT_ACCEPTABLE);
        }
    }
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
    @RequestMapping(value = "/vertical/horizontal/update/{applicationId}", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateVerticalAndHorizontal(@PathVariable final int applicationId,@RequestBody String verticalhorizontal) {
        log.debug("Receive request to update HorizontalVerticalName with id {}", applicationId);
        boolean status=false;
        try {
        	if(verticalhorizontal!=null && !("").equalsIgnoreCase(verticalhorizontal)) {
        		verticalService.updateVerticalHorizontalApplication(verticalhorizontal,applicationId);
        	}
        	else {
        		new ResponseEntity("{\"Response\":\"Invalid horizontalName & verticalName \"}", HttpStatus.NOT_ACCEPTABLE);
        	}
            return new ResponseEntity(status, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Exception occurred to get HorizontalVerticalName {0}", applicationId + " : " + e.getMessage());
            return new ResponseEntity("{\"Response\":\"Invalid application ID\"}", HttpStatus.NOT_ACCEPTABLE);
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
    @RequestMapping(value = "/add", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity saveVerticalName(@RequestParam String addvertical){
        log.debug("Receive request to saveVerticalName");
        try {
        	List<Map<String,Object>> verticalhorizontallist=null;
        	if(addvertical!=null && !("").equals(addvertical)) {
        		verticalhorizontallist =  verticalService.saveVertical(addvertical);
        	}else {
        		throw new Exception("Invalid vertical name");
        	}
        	return new ResponseEntity(verticalhorizontallist, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception occurred to save VerticalName " + e.getMessage());
            return new ResponseEntity("{\"Response\":\"Invalid vertical Name\"}", HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
