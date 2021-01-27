package com.tmobile.cso.vault.api.model;

import java.io.Serializable;

public class SafeDetails implements Serializable {
	
	private static final long serialVersionUID = 1928776631283008580L;
	/**
	 * Safe lastmodified person
	 */
	private Long modifiedat;
	/**
	 * Safe lastmodified time
	 */
	/**
	 * LastModifiedby
	 */
	private String modifiedby;
	
	public SafeDetails() {
		super();
		
	}

	public SafeDetails(Long modifiedat, String modifiedby) {
		super();
		this.modifiedat = modifiedat;
		this.modifiedby = modifiedby;
	}

	/**
	 * 
	 * @return modifiedat
	 */
	public Long getModifiedat() {
		return modifiedat;
	}

	/**
	 * 
	 * @param modifiedat
	 */
	public void setModifiedat(Long modifiedat) {
		this.modifiedat = modifiedat;
	}
   
	/**
	 * 
	 * @return modifiedby
	 */
	public String getModifiedby() {
		return modifiedby;
	}

	/**
	 * 
	 * @param modifiedby
	 */
	public void setModifiedby(String modifiedby) {
		this.modifiedby = modifiedby;
	}
	
	
	@Override
	public String toString() {
		return "SafeDetails [modifiedat=" + modifiedat + ", modifiedby=" + modifiedby + "]";
	}
	
	
}
