package com.ams.model;

import java.time.LocalDate;

public class EmployeeDetails {
	
	private String employeeId;
	private String employeeName;
	private LocalDate date;
	private String totalTime;
	private int outCount;
	private String totalOutingTime;
	
	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	
	public String getTotalOutingTime() {
		return totalOutingTime;
	}

	public void setTotalOutingTime(String totalOutingTime) {
		this.totalOutingTime = totalOutingTime;
	}
	
	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	// Getters and setters
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}

	public int getOutCount() {
		return outCount;
	}

	public void setOutCount(int outCount) {
		this.outCount = outCount;
	}

}
