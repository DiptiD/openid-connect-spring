package com.gracenote.content.auth.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "horizontal")
public class Horizontal{
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	@Id
    @Column(updatable = false, nullable = false, name= "horizontal_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
    
    @Column(name = "horizontal_name")
	private String horizontalName;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHorizontalName() {
		return horizontalName;
	}

	public void setHorizontalName(String horizontalName) {
		this.horizontalName = horizontalName;
	}
}
