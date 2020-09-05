package com.gracenote.content.auth.domain.user;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller to test the dashboard authorisation.
 *
 * @author Vasudevan on 08/09/17.
 */
@FrameworkEndpoint
@Controller
@EnableResourceServer
public class DashboardController {

	@Autowired
	ConsumerTokenServices tokenServices;
	 
	@RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login(Model model, String error, String logout) {
        return new ModelAndView("login");
    }
    
	@RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView index(Model model, String error, String logout) {
        return new ModelAndView("home");
    }
	
	@RequestMapping(value = "/dashboard", method = RequestMethod.POST)
    public ModelAndView Dashboard(Model model, String error, String logout) {
        return new ModelAndView("dashboard");
    }
	
	@RequestMapping(value = "/admin", method = RequestMethod.GET)
    public ModelAndView admin(Model model, String error, String logout) {
        return new ModelAndView("admin");
    }
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/oauth/logout")
    @ResponseBody
    public ResponseEntity revokeToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.contains("Bearer")) {
            String tokenId = authorization.substring("Bearer".length() + 1);
            if(tokenServices.revokeToken(tokenId))
                return new ResponseEntity(true, HttpStatus.OK);
            else
                return new ResponseEntity(false, HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity(false, HttpStatus.BAD_REQUEST);
    }
}
