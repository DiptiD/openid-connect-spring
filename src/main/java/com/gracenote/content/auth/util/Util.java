package com.gracenote.content.auth.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.gracenote.content.auth.persistence.entity.ExceptionVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Utility function used across the function
 *
 * @author deepak on 12/9/17.
 */

public class Util {

    private static final Logger log = LoggerFactory.getLogger(Util.class);

    /**
     * Validate string against null and empty
     *
     * @param str
     * @return true if passed string is neither null or empty else false
     */
    public static boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }

    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static ResponseEntity exceptionHandler(String error,String errorDescription,HttpStatus status) {
    	ExceptionVO exceptionvo=new ExceptionVO();
    	exceptionvo.setError(error);
    	exceptionvo.setError_description(errorDescription);
        return new ResponseEntity(exceptionvo, status);
    }

    public static boolean uploadImage(MultipartFile file, String imagePath) {

        if(!file.isEmpty()) {
            String name = file.getOriginalFilename();
            //File destinationFile = new File("/var/www/html/content-auth/assets/img"+  File.separator + name);
            try {
                //file.transferTo(destinationFile);
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(name)));
                stream.write(bytes);
                stream.close();

                return true;
            } catch (Exception e) {
                log.error("Exception occurred to upload application image file : "+e.getMessage());
            }
        }
        return false;
    }
}
