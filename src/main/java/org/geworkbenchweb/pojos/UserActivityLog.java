/**
 * 
 */
package org.geworkbenchweb.pojos;

import javax.persistence.Entity;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

/**
 * @author zji
 *
 */
@Entity
public class UserActivityLog extends AbstractPojo {

	private static final long serialVersionUID = -6260943298409829347L;
	private String username;
	private String activityType;
	private String auxiliaryInfo;
	private java.sql.Timestamp timestamp;
	
	public static enum ACTIVITY_TYPE {LOG_IN, LOG_OUT, ANALYSIS, RESULT, LOAD_DATA}
	
	public UserActivityLog() {
	}
	
	public UserActivityLog(String username, String activityType, String auxiliaryInfo) {
		this.username = username;
		this.activityType = activityType;
		this.auxiliaryInfo = auxiliaryInfo;
		timestamp = new java.sql.Timestamp(System.currentTimeMillis());
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getActivityType() {
		return activityType;
	}
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}
	public String getAuxiliaryInfo() {
		return auxiliaryInfo;
	}
	public void setAuxiliaryInfo(String auxiliaryInfo) {
		this.auxiliaryInfo = auxiliaryInfo;
	}
	public java.sql.Timestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(java.sql.Timestamp timestamp) {
		this.timestamp = timestamp;
	}
}
