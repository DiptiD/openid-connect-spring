package com.gracenote.content.auth.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "horizontal_application")
public class HorizontalApplication{

	@Id
    @Column(updatable = false, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

	@Column(name="application_id")
	private int applicationId;
	
	@Column(name="horizontal_id")
	private int horizontalId;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", insertable = false, updatable = false)
    private Application application;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horizontal_id", insertable = false, updatable = false)
    private Horizontal horizontal;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	public int getHorizontalId() {
		return horizontalId;
	}

	public void setHorizontalId(int horizontalId) {
		this.horizontalId = horizontalId;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public Horizontal getHorizontal() {
		return horizontal;
	}

	public void setHorizontal(Horizontal horizontal) {
		this.horizontal = horizontal;
	}    
}