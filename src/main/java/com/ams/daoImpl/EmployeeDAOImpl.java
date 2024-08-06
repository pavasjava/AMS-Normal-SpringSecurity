package com.ams.daoImpl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.ams.dao.EmployeeDao;
import com.ams.model.Employee;
import com.ams.model.EmployeeDetails;

@Repository
public class EmployeeDAOImpl implements EmployeeDao {
//	@Autowired
//	private DataSource dataSource;

	@Autowired
	@Qualifier("primaryDataSource")
	private DataSource dataSource;

	@Autowired
	@Qualifier("secondaryDataSource")
	private DataSource secondaryService;

	@Override
	public List<Employee> getAllEmployees() {
		List<Employee> employees = new ArrayList<>();
		try (Connection connection = dataSource.getConnection()) {
			// String sql = "SELECT * FROM Employee";
			String sql = "SELECT *,(select toggle_state from attendance where employee_id=Employee.employee_id order by attendance_id desc limit 1) togglestate FROM Employee";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				Employee employee = new Employee();
				employee.setEmployeeId(resultSet.getString("employee_id"));
				employee.setEmployeeName(resultSet.getString("employee_name"));
				employee.setEmailId(resultSet.getString("email_id"));
				employee.setDesignation(resultSet.getString("designation"));
				employee.setToggleState(resultSet.getString("togglestate"));
//				employee.setPhoneNO(resultSet.getString("phone_no"));
				employees.add(employee);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employees;
	}

	@Override
	public String getEmployeeToggleState(Long employeeId) {
		String toggleState = null;
		try (Connection connection = dataSource.getConnection()) {
			String sql = "SELECT toggle_state FROM Employee WHERE employee_id = ?";
			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				preparedStatement.setLong(1, employeeId);
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						toggleState = resultSet.getString("toggle_state");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Error fetching toggle state", e);
		}
		return toggleState;
	}

	@Override
	public void addEmployee(Employee employee) {
		try (Connection connection = dataSource.getConnection()) {
			String sql = "INSERT INTO Employee (employee_id, employee_name, email_id, designation) VALUES (?, ?, ?, ?)";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, employee.getEmployeeId());
			preparedStatement.setString(2, employee.getEmployeeName());
			preparedStatement.setString(3, employee.getEmailId());
			preparedStatement.setString(4, employee.getDesignation());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateEmployee(Employee employee) {
		try (Connection connection = dataSource.getConnection()) {
			String sql = "UPDATE Employee SET employee_name = ?, email_id = ?, designation = ?, in_time = ?, out_time = ? WHERE employee_id = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, employee.getEmployeeName());
			preparedStatement.setString(2, employee.getEmailId());
			preparedStatement.setString(3, employee.getDesignation());
			preparedStatement.setTimestamp(4, Timestamp.valueOf(employee.getInTime()));
			preparedStatement.setTimestamp(5, Timestamp.valueOf(employee.getOutTime()));
			preparedStatement.setString(6, employee.getEmployeeId());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void toggleTime(Long employeeId, String action) {
		try (Connection connection = dataSource.getConnection()) {
			if (action.equals("in")) {
				// Insert a new row into the Attendance table with toggle_state as 'out'
//				String insertAttendanceSql = "INSERT INTO attendancemonitoring.attendance (employee_id, employee_name, in_time, out_time, toggle_state)"
//						+ "SELECT e.employee_id, e.employee_name, '2024-06-03 09:00:00', NULL, 'out'"
//						+ "FROM attendancemonitoring.employee e"
//						+ "WHERE e.employee_name = 'Rakesh Jena'"
				String insertAttendanceSql = "INSERT INTO Attendance (employee_id, in_time, out_time, toggle_state, date) VALUES (?, ?, NULL, 'out', ?)";
				try (PreparedStatement insertAttendanceStatement = connection.prepareStatement(insertAttendanceSql)) {
					LocalDateTime now = LocalDateTime.now();
					Timestamp newTime = Timestamp.valueOf(now);
					LocalDate currentDate = now.toLocalDate();
					insertAttendanceStatement.setLong(1, employeeId);
					insertAttendanceStatement.setTimestamp(2, newTime);
					insertAttendanceStatement.setDate(3, java.sql.Date.valueOf(currentDate)); // Set the date
					insertAttendanceStatement.executeUpdate();
				}
				// Update the toggle_state in the Employee table
				String updateEmployeeSql = "UPDATE Employee SET toggle_state = 'out' WHERE employee_id = ?";
				try (PreparedStatement updateEmployeeStatement = connection.prepareStatement(updateEmployeeSql)) {
					updateEmployeeStatement.setLong(1, employeeId);
					updateEmployeeStatement.executeUpdate();
				}
			} else if (action.equals("out")) {
				// Update the last row in the Attendance table for the specified employee with
				// toggle_state as 'in'
				String updateAttendanceSql = "UPDATE Attendance SET out_time = ?, toggle_state = 'in' WHERE employee_id = ? AND out_time IS NULL ORDER BY in_time DESC LIMIT 1";
				try (PreparedStatement updateAttendanceStatement = connection.prepareStatement(updateAttendanceSql)) {
					Timestamp newTime = Timestamp.valueOf(LocalDateTime.now());
					updateAttendanceStatement.setTimestamp(1, newTime);
					updateAttendanceStatement.setLong(2, employeeId);
					int rowsUpdated = updateAttendanceStatement.executeUpdate();
					if (rowsUpdated == 0) {
						throw new RuntimeException("No matching 'IN' record found for employee: " + employeeId);
					}
				}
				// Update the toggle_state in the Employee table
				String updateEmployeeSql = "UPDATE Employee SET toggle_state = 'in' WHERE employee_id = ?";
				try (PreparedStatement updateEmployeeStatement = connection.prepareStatement(updateEmployeeSql)) {
					updateEmployeeStatement.setLong(1, employeeId);
					updateEmployeeStatement.executeUpdate();
				}
			} else {
				throw new IllegalArgumentException("Invalid action specified.");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error updating employee time", e);
		}
	}

//	@Override
//	public void toggleTime(Long employeeId, String action) {
//		try (Connection connection = dataSource.getConnection()) {
//			if (action.equals("in")) {
//				// Insert a new row into the Attendance table with toggle_state as 'out'
////				String insertAttendanceSql = "INSERT INTO attendancemonitoring.attendance (employee_id, employee_name, in_time, out_time, toggle_state)"
////						+ "SELECT e.employee_id, e.employee_name, '2024-06-03 09:00:00', NULL, 'out'"
////						+ "FROM attendancemonitoring.employee e"
////						+ "WHERE e.employee_name = 'Rakesh Jena'"
//				String insertAttendanceSql = "INSERT INTO Attendance (employee_id, in_time, out_time, toggle_state) VALUES (?, ?, NULL, 'out')";
//				try (PreparedStatement insertAttendanceStatement = connection.prepareStatement(insertAttendanceSql)) {
//					Timestamp newTime = Timestamp.valueOf(LocalDateTime.now());
//					insertAttendanceStatement.setLong(1, employeeId);
//					insertAttendanceStatement.setTimestamp(2, newTime);
//					insertAttendanceStatement.executeUpdate();
//				}
//				// Update the toggle_state in the Employee table
//				String updateEmployeeSql = "UPDATE Employee SET toggle_state = 'out' WHERE employee_id = ?";
//				try (PreparedStatement updateEmployeeStatement = connection.prepareStatement(updateEmployeeSql)) {
//					updateEmployeeStatement.setLong(1, employeeId);
//					updateEmployeeStatement.executeUpdate();
//				}
//			} else if (action.equals("out")) {
//				// Update the last row in the Attendance table for the specified employee with
//				// toggle_state as 'in'
//				String updateAttendanceSql = "UPDATE Attendance SET out_time = ?, toggle_state = 'in' WHERE employee_id = ? AND out_time IS NULL ORDER BY in_time DESC LIMIT 1";
//				try (PreparedStatement updateAttendanceStatement = connection.prepareStatement(updateAttendanceSql)) {
//					Timestamp newTime = Timestamp.valueOf(LocalDateTime.now());
//					updateAttendanceStatement.setTimestamp(1, newTime);
//					updateAttendanceStatement.setLong(2, employeeId);
//					int rowsUpdated = updateAttendanceStatement.executeUpdate();
//					if (rowsUpdated == 0) {
//						throw new RuntimeException("No matching 'IN' record found for employee: " + employeeId);
//					}
//				}
//				// Update the toggle_state in the Employee table
//				String updateEmployeeSql = "UPDATE Employee SET toggle_state = 'in' WHERE employee_id = ?";
//				try (PreparedStatement updateEmployeeStatement = connection.prepareStatement(updateEmployeeSql)) {
//					updateEmployeeStatement.setLong(1, employeeId);
//					updateEmployeeStatement.executeUpdate();
//				}
//			} else {
//				throw new IllegalArgumentException("Invalid action specified.");
//			}
//		} catch (SQLException e) {
//			throw new RuntimeException("Error updating employee time", e);
//		}
//	}

	public List<EmployeeDetails> getEmployeeDetails(Long employeeId) {
		List<EmployeeDetails> details = new ArrayList<>();
		String sql = "SELECT e.employee_name, DATE(a.in_time) AS date, "
				+ "SEC_TO_TIME(SUM(TIMESTAMPDIFF(SECOND, a.in_time, IFNULL(a.out_time, NOW())))) AS total_time, "
				+ "COUNT(a.out_time) AS out_count " + "FROM Attendance a "
				+ "JOIN Employee e ON a.employee_id = e.employee_id WHERE a.employee_id = ? "
				+ "GROUP BY e.employee_name, DATE(a.in_time)";
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, employeeId);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				EmployeeDetails detail = new EmployeeDetails();
				detail.setDate(resultSet.getDate("date").toLocalDate());
				// Retrieving total time as a string in the format 'HH:MM:SS'
				String totalTime = resultSet.getString("total_time");
				detail.setTotalTime(totalTime);
				detail.setOutCount(resultSet.getInt("out_count"));
				detail.setEmployeeName(resultSet.getString("employee_name"));
				details.add(detail);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Error fetching employee details", e);
		}
		return details;
	}
	@Override
	public List<EmployeeDetails> getEmployeeDetailsByCurrentMonth(Long employeeId) {
	    List<EmployeeDetails> details = new ArrayList<>();
	    String sql = "SELECT e.employee_name, DATE(a.date) AS date, " +
	            "SEC_TO_TIME(SUM(TIMESTAMPDIFF(SECOND, a.in_time, IFNULL(a.out_time, NOW())))) AS total_time, " +
	            "COUNT(a.out_time) AS out_count " +
	            "FROM Attendance a " +
	            "JOIN Employee e ON a.employee_id = e.employee_id " +
	            "WHERE a.employee_id = ? " +
	            "GROUP BY e.employee_name, DATE(a.date);";

	    try (Connection connection = dataSource.getConnection();
	         PreparedStatement statement = connection.prepareStatement(sql)) {
	        statement.setLong(1, employeeId);
	        ResultSet resultSet = statement.executeQuery();
	        while (resultSet.next()) {
	            EmployeeDetails detail = new EmployeeDetails();
	            detail.setDate(resultSet.getDate("date").toLocalDate());
	            detail.setTotalTime(resultSet.getString("total_time"));
	            detail.setOutCount(resultSet.getInt("out_count"));
	            detail.setEmployeeName(resultSet.getString("employee_name"));
	            details.add(detail);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        throw new RuntimeException("Error fetching employee details", e);
	    }
	    return details;
	}

	
	

	@Override
	public List<EmployeeDetails> getAllEmployeesDataForCurrentDate() {
		List<EmployeeDetails> details = new ArrayList<>();
		String sql = "SELECT e.employee_name, DATE(a.in_time) AS date, SEC_TO_TIME(SUM(TIMESTAMPDIFF(SECOND, a.in_time, IFNULL(a.out_time, NOW())))) AS total_time, "
				+ "    COUNT(a.out_time) AS out_count FROM  attendance a  JOIN  "
				+ "    Employee e ON a.employee_id = e.employee_id  WHERE  "
				+ "    MONTH(a.in_time) = MONTH(CURRENT_DATE())  "
				+ "    AND YEAR(a.in_time) = YEAR(CURRENT_DATE()) AND DATE(a.in_time) = CURDATE()  "
				+ "GROUP BY e.employee_name, DATE(a.in_time); ";
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				EmployeeDetails detail = new EmployeeDetails();
				detail.setDate(resultSet.getDate("date").toLocalDate());
				// Retrieving total time as a string in the format 'hh:mm:ss'
				String totalTime = resultSet.getString("total_time");
				detail.setTotalTime(totalTime);
				detail.setOutCount(resultSet.getInt("out_count"));
				// Set employee name
				detail.setEmployeeName(resultSet.getString("employee_name"));
				details.add(detail);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Error fetching employee details", e);
		}
		return details;
	}

	// Method to fetch employee name for a given date
	private String getEmployeeNameForDate(LocalDate date) {
		String employeeName = null;
		String sql = "SELECT e.employee_name FROM attendance a " + "JOIN Employee e ON a.employee_id = e.employee_id "
				+ "WHERE DATE(a.in_time) = ?";
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setDate(1, Date.valueOf(date));
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				employeeName = resultSet.getString("employee_name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Error fetching employee name", e);
		}
		return employeeName;
	}

	@Override
	public List<Employee> getAllEmployeesForDropDown() {
		List<Employee> employees = new ArrayList<>();
		try (Connection connection = secondaryService.getConnection()) {
			String sql = "select emp_id, concat(f_name,' ', m_name,' ', l_name) emp_name from emp_mast where  cur_off_code='OLSGAD0010001'";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				Employee employee = new Employee();
				employee.setEmployeeId(resultSet.getString("emp_id"));
				employee.setEmployeeName(resultSet.getString("emp_name"));
				employees.add(employee);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employees;
	}

	@Override
	public Employee getEmployeeNameForDropDown(Long employeeId) {
		Employee employee = null;
		String sql = "select emp_id, concat(f_name,' ', m_name,' ', l_name) employeeName from emp_mast where  cur_off_code='OLSGAD0010001' and  emp_id = CAST(? AS VARCHAR)";
		try (Connection connection = secondaryService.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, employeeId);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				employee = new Employee();
				employee.setEmployeeName(resultSet.getString("employeeName"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employee;
	}

	@Override
	public List<EmployeeDetails> getEmployeeDetails(Long employeeId, LocalDate startDate, LocalDate endDate) {
		List<EmployeeDetails> details = new ArrayList<>();
		String sql = "SELECT e.employee_name, DATE(a.in_time) AS date, "
				+ "SEC_TO_TIME(SUM(TIMESTAMPDIFF(SECOND, a.in_time, IFNULL(a.out_time, NOW())))) AS total_time, "
				+ "COUNT(a.out_time) AS out_count FROM Attendance a "
				+ "JOIN Employee e ON a.employee_id = e.employee_id WHERE a.employee_id = ? "
				+ "AND DATE(a.in_time) BETWEEN ? AND ? " + "GROUP BY e.employee_name, DATE(a.in_time)";
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, employeeId);
			statement.setDate(2, java.sql.Date.valueOf(startDate));
			statement.setDate(3, java.sql.Date.valueOf(endDate));
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				EmployeeDetails detail = new EmployeeDetails();
				detail.setDate(resultSet.getDate("date").toLocalDate());
				// Retrieving total time as a string in the format 'HH:MM:SS'
				String totalTime = resultSet.getString("total_time");
				detail.setTotalTime(totalTime);
				detail.setOutCount(resultSet.getInt("out_count"));
				detail.setEmployeeName(resultSet.getString("employee_name"));
				details.add(detail);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Error fetching employee details", e);
		}
		return details;
	}
	
	
	@Override
	public List<Employee> getTotalOutingTime(Long attendanceId) {
	    String query = "SELECT employee_id,DATE(date) AS attendance_date,SEC_TO_TIME(SUM(TIME_TO_SEC(TIMEDIFF(out_time, in_time)))) AS total_outside_time "
	    		+ "FROM attendance WHERE employee_id = ? AND toggle_state = 'in' AND DATE(date) BETWEEN CURDATE() - INTERVAL 60 DAY AND CURDATE() "
	    		+ "GROUP BY employee_id, DATE(date) ORDER BY attendance_date;";

	    List<Employee> employeeDetails = new ArrayList<>();
	    System.out.println("Starting query execution");

	    try (Connection connection = dataSource.getConnection();
	         PreparedStatement statement = connection.prepareStatement(query)) {

	        statement.setLong(1, attendanceId);
	        System.out.println("Prepared statement created with attendanceId: " + attendanceId);

	        try (ResultSet resultSet = statement.executeQuery()) {
	            System.out.println("Query executed");

	            while (resultSet.next()) { 
	                Employee employee = new Employee();
	                String totalOutingTime = resultSet.getString("total_outside_time");
	                employee.setTotalOutingTime(totalOutingTime);
	                System.out.println("DAO Total Outing Time -> " + totalOutingTime);

	                // Print more details if needed
	                System.out.println("Employee ID: " + resultSet.getLong("employee_id"));
	                System.out.println("Date: " + resultSet.getDate("date"));
	                System.out.println("Outing Count: " + resultSet.getInt("outing_count"));

	                // Set other fields if necessary
	                employeeDetails.add(employee); // Add employee to list
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        throw new RuntimeException("Error fetching total outing time for attendance ID: " + attendanceId, e);
	    }

	    System.out.println("Query execution finished");
	    System.out.println("Total records fetched: " + employeeDetails.size());
	    return employeeDetails;
	}
}