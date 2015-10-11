package com.models;

import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class ResponseStore implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3993047164260406504L;
	private Store store;
	private Status status;
	
	
	public ResponseStore() {
		
	}
	
	public void setStore(Store store) {
	    this.store = store;
	}
	
	public Store getStore() {
	    return store;
	}
	
	public void setStatus(Status status) {
	    this.status = status;
	}
	
	public Status getStatus() {
	    return status;
	}
	
	
}
