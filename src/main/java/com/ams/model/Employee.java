package com.ams.model;

import java.time.LocalDateTime;

public class Employee {

	private String employeeId;
	private String employeeName;
	private String emailId;
	private String designation;
	private LocalDateTime date;
	private LocalDateTime inTime; 
    private LocalDateTime outTime;
    private String toggleState;
    private String totalOutingTime;
//    private Double totalWorkingHr;
//    private String status;
//    private Integer outCount;
	
	public String getTotalOutingTime() {
		return totalOutingTime;
	}

	public void setTotalOutingTime(String totalOutingTime) {
		this.totalOutingTime = totalOutingTime;
	}

	public String getToggleState() {
		return toggleState;
	}

	public void setToggleState(String toggleState) {
		this.toggleState = toggleState;
	}

	public Employee() {

	}
	
	public String getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	public String getEmployeeName() {
		return employeeName;
	}
	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public String getDesignation() {
		return designation;
	}
	public void setDesignation(String designation) {
		this.designation = designation;
	}
	
	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public LocalDateTime getInTime() {
		return inTime;
	}

	public void setInTime(LocalDateTime inTime) {
		this.inTime = inTime;
	}

	public LocalDateTime getOutTime() {
		return outTime;
	}

	public void setOutTime(LocalDateTime outTime) {
		this.outTime = outTime;
	}

//	public Double getTotalWorkingHr() {
//		return totalWorkingHr;
//	}
//
//	public void setTotalWorkingHr(Double totalWorkingHr) {
//		this.totalWorkingHr = totalWorkingHr;
//	}
//
//	public String getStatus() {
//		return status;
//	}
//
//	public void setStatus(String status) {
//		this.status = status;
//	}
//
//	public Integer getOutCount() {
//		return outCount;
//	}
//
//	public void setOutCount(Integer outCount) {
//		this.outCount = outCount;
//	}
}
