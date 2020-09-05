package com.gracenote.content.auth.domain.horizontal;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gracenote.content.auth.persistence.entity.Horizontal;

@RestController
@EnableResourceServer
@RequestMapping(value = "api/v1/horizontals")
public class HorizontalController {

	private final Logger log = LoggerFactory.getLogger(HorizontalController.class);
	
	@Autowired
	HorizontalService horizontalService;
	
	/**
     * Get list of all verticals
     *
     * @return list of horizontal.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "", method = GET)
    public ResponseEntity getHorizontals() {
        try {
        	
            List<Horizontal> horizontal = horizontalService.getAllHorizontals();
            return new ResponseEntity(horizontal, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Exception occurred at get all applications: " + e.getMessage());
            return new ResponseEntity("{\"Response\":\"No application found\"}", HttpStatus.NOT_FOUND);
        }
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(value = "/add", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity saveVerticalName(@RequestParam String addhorizontal){
        log.debug("Receive request to saveHorizontalName");
        try {
        	List<Map<String,Object>> verticalhorizontallist=null;
        	if(addhorizontal!=null && !("").equalsIgnoreCase(addhorizontal)) {
        	  verticalhorizontallist =  horizontalService.saveHorizontal(addhorizontal);
        	}else{
        		throw new Exception("Invalid horizontal name");
        	}
            return new ResponseEntity(verticalhorizontallist, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception occurred to save saveHorizontalName " + e.getMessage());
            return new ResponseEntity("{\"Response\":\"Invalid saveHorizontal name\"}", HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
