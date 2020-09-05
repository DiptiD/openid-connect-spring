package com.gracenote.content.auth.persistence.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "vertical")
public class Vertical{
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	@Id
    @Column(updatable = false, nullable = false,name= "vertical_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
    
    @Column(name = "vertical_name")
	private String verticalName;
    
    @ManyToMany(
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL
    )
    @JoinTable(
            name = "vertical_horizontal_application",
            joinColumns = @JoinColumn(name = "vertical_id"),
            inverseJoinColumns = @JoinColumn(name = "id"))
    private List<HorizontalApplication> horizontalApplication=new ArrayList<>();
    
	public List<HorizontalApplication> getHorizontalApplication() {
		return horizontalApplication;
	}

	public void setHorizontalApplication(List<HorizontalApplication> horizontalApplication) {
		this.horizontalApplication = horizontalApplication;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getVerticalName() {
		return verticalName;
	}

	public void setVerticalName(String verticalName) {
		this.verticalName = verticalName;
	}
}
