package com.gracenote.content.auth.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.web.multipart.MultipartFile;

/*import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;*/

public class S3ImageFileUploadAndDownload {
	/*
	 * 
	 * 
	 * public Properties PropertiesCredentials(InputStream inputStream) throws
	 * IOException { Properties accountProperties = new Properties(); try {
	 * accountProperties.load(inputStream); } finally { try {inputStream.close();}
	 * catch (Exception e) {} } if (accountProperties.getProperty("access") == null
	 * || accountProperties.getProperty("secret") == null) { throw new
	 * IllegalArgumentException("The specified properties data " +
	 * "doesn't contain the expected properties 'accessKey' and 'secretKey'."); }
	 * return accountProperties; }
	 * 
	 * public static AmazonS3 getConfiguration() throws IOException {
	 * PropertiesCredentials credential=new PropertiesCredentials(new
	 * File("/var/lib/tomcat7/webapps/AwsCredentials.properties")); AmazonS3
	 * s3client = AmazonS3ClientBuilder .standard() .withCredentials(new
	 * AWSStaticCredentialsProvider(credential)) .withRegion(Regions.US_WEST_2)
	 * .build(); return s3client; }
	 * 
	 * private static File convertMultiPartToFile(MultipartFile file) throws
	 * IOException { File convFile = new
	 * File("/home/ssojenkinsbot/imageuploads/"+file.getOriginalFilename());
	 * FileOutputStream fos = new FileOutputStream(convFile);
	 * fos.write(file.getBytes()); fos.close(); return convFile; }
	 * 
	 * 
	 * public static boolean uploadImageFile(MultipartFile file) throws IOException{
	 * File filePath=convertMultiPartToFile(file); String bucketName =
	 * Constants.SSO_APPLICATION_BUCKET_NAME;
	 * 
	 * String imageName=""; if(filePath.exists()) imageName= filePath.getName();
	 * else { return false; } Boolean status=Boolean.FALSE; try { AmazonS3 s3client
	 * = getConfiguration(); FileInputStream stream = new FileInputStream(filePath);
	 * ObjectMetadata objectMetadata = new ObjectMetadata(); PutObjectRequest
	 * putObjectRequest = new PutObjectRequest(bucketName, imageName, stream,
	 * objectMetadata) .withCannedAcl(CannedAccessControlList.PublicRead);
	 * s3client.putObject(putObjectRequest);
	 * System.out.println(s3client.getUrl(bucketName,filePath.getPath()));
	 * status=Boolean.TRUE; }catch(Exception e) { status=Boolean.FALSE; //throw new
	 * Exception("Failed to upload an image with error message "+e.getMessage()); }
	 * return status; }
	 * 
	 * public static void main(String args[]) throws IOException { AmazonS3 s3client
	 * = getConfiguration(); }
	 */}
