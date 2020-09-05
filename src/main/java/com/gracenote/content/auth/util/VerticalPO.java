package com.gracenote.content.auth.util;

import java.util.List;
import java.util.Map;

import com.gracenote.content.auth.persistence.entity.Application;

public class VerticalPO {

	private String vertical;
	private Map<String,Object> horizontal;
	public String getVertical() {
		return vertical;
	}
	public void setVertical(String vertical) {
		this.vertical = vertical;
	}
	public Map<String, Object> getHorizontal() {
		return horizontal;
	}
	public void setHorizontal(Map<String, Object> horizontal) {
		this.horizontal = horizontal;
	}
	
}
