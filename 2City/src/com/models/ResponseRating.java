package com.models;

import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class ResponseRating implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3993047164260406504L;
	private Rating store_rating;
	private Status status;
	
	
	public ResponseRating() {
		
	}
	
	public void setStore_rating(Rating store_rating) {
	    this.store_rating = store_rating;
	}
	
	public Rating getStore_rating() {
	    return store_rating;
	}
	
	public void setStatus(Status status) {
	    this.status = status;
	}
	
	public Status getStatus() {
	    return status;
	}
	
	
}
