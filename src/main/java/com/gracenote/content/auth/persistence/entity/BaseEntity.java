package com.gracenote.content.auth.persistence.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Represents the super base entity class.
 *
 * @author Vasudevan on 07/09/17.
 */
@MappedSuperclass
public class BaseEntity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Column(name="createdTime")
	Timestamp createdTime;

	public Timestamp getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Timestamp timestamp) {
		this.createdTime = timestamp;
	}
}
