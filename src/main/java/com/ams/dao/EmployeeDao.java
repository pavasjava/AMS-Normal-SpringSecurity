package com.ams.dao;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import com.ams.model.Employee;
import com.ams.model.EmployeeDetails;

public interface EmployeeDao {

	List<Employee> getAllEmployees();

	void addEmployee(Employee employee);

	void updateEmployee(Employee employee);
	public void toggleTime(Long employeeId, String action);
	public List<EmployeeDetails> getEmployeeDetails(Long empId);
	public String getEmployeeToggleState(Long employeeId);
	public List<EmployeeDetails> getEmployeeDetailsByCurrentMonth(Long employeeId);
	public List<EmployeeDetails> getAllEmployeesDataForCurrentDate();
	public List<Employee> getAllEmployeesForDropDown();
	public Employee getEmployeeNameForDropDown(Long employeeId);
	public List<EmployeeDetails> getEmployeeDetails(Long employeeId, LocalDate startDate, LocalDate endDate);
	
	public List<Employee> getTotalOutingTime(Long attendanceId);
//	public List<EmployeeDetails> getTotalOutingTime(Long attendanceId);
	
//	List<Employee> findAllActiveEmployees();
//    void markInTime(String empId);
//    void markOutTime(String empId);

}
