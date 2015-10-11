package com.models;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class DataResponse {
	
//	@JsonTypeInfo(  
//	        use = JsonTypeInfo.Id.NAME,  
//	        include = JsonTypeInfo.As.WRAPPER_OBJECT)  
//	    @JsonSubTypes({  
//	        @Type(value = Status.class, name = "status")})  
	private Status status;
	
//	@JsonTypeInfo(  
//	        use = JsonTypeInfo.Id.NAME,  
//	        include = JsonTypeInfo.As.WRAPPER_OBJECT)  
//	    @JsonSubTypes({  
//	        @Type(value = User.class, name = "user_info")})  
	private User user_info;
	
	private User photo_user_info;
	

	public DataResponse() {
		
	}
	
	public void setStatus(Status status) {
	    this.status = status;
	}
	
	public Status getStatus() {
	    return status;
	}
	
	public void setUser_info(User user_info) {
		this.user_info = user_info;
	}
	
	public User getUser_info() {
	    return user_info;
	}
	
	public void setPhoto_user_info(User photo_user_info) {
		this.photo_user_info = photo_user_info;
	}
	
	public User getPhoto_user_info() {
	    return photo_user_info;
	}
	
	
}
