package com.ams.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ams.dao.EmployeeDao;
import com.ams.model.Employee;
import com.ams.model.EmployeeDetails;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/employees")
public class EmployeeController {
	
	@Autowired
    private EmployeeDao employeeDAO;

    @GetMapping("/getAllEmployee")
    public String listEmployees(Model model) {
        List<Employee> employees = employeeDAO.getAllEmployees();
        model.addAttribute("employees", employees);
        return "employee-list";
    }
    
    @GetMapping("/getEmployeeName/{employeeId}")
    public ResponseEntity<Employee> getEmployeeNameForDropDown(@PathVariable Long employeeId) {
        Employee employee = employeeDAO.getEmployeeNameForDropDown(employeeId);
        if (employee != null) {
            return ResponseEntity.ok(employee);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/add")
    public String addEmployeeForm(Model model) {
    	List<Employee> employees = employeeDAO.getAllEmployeesForDropDown();
    	model.addAttribute("employees", employees);
        model.addAttribute("employee", new Employee());
        return "employee-form";
    }

    @PostMapping("/save")
    public String addEmployee(@ModelAttribute Employee employee) {
    	employeeDAO.addEmployee(employee);
        return "redirect:/employees/getAllEmployee";
    }

    @GetMapping("/edit/{empId}")
    public String editEmployeeForm(@PathVariable String empId, Model model) {
        Employee employee = employeeDAO.getAllEmployees().stream()
                                           .filter(e -> e.getEmployeeId() == empId)
                                           .findFirst()
                                           .orElse(null);
        model.addAttribute("employee", employee);
        return "employee-form";
    }

    @PostMapping("/edit")
    public String updateEmployee(@ModelAttribute Employee employee) {
    	employeeDAO.updateEmployee(employee);
        return "redirect:/employees";
    }
    @PostMapping("/toggle-time")
    @ResponseBody
    public void toggleTime(@RequestBody Map<String, Object> payload, HttpServletResponse response) {
        try {
            Long employeeId = Long.parseLong(payload.get("employeeId").toString());
            String action = payload.get("action").toString();
            employeeDAO.toggleTime(employeeId, action);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("Error processing request: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    
    @GetMapping("/{employeeId}/details")
    public String viewDetails(@PathVariable Long employeeId, Model model) {
        List<EmployeeDetails> employeeDetails = employeeDAO.getEmployeeDetailsByCurrentMonth(employeeId);
//        List<Employee> totalOutingTime = employeeDAO.getTotalOutingTime(employeeId);
        for (EmployeeDetails detail : employeeDetails) {
            System.out.println("Employee Name: " + detail.getEmployeeName());
            System.out.println("Date: " + detail.getDate());
            System.out.println("Total Minutes: " + detail.getTotalTime());
            System.out.println("Out Count: " + detail.getOutCount());
            System.out.println("------------------------------");
        }
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("employeeDetails", employeeDetails);
//        model.addAttribute("totalOutingTime", totalOutingTime);
        return "employee-details";
    }

    
//    @GetMapping("/{employeeId}/details")
//    public String viewDetails(@PathVariable Long employeeId, Model model) {
//        List<EmployeeDetails> employeeDetails = employeeDAO.getEmployeeDetails(employeeId);
//        List<Employee> totalOutingTime = employeeDAO.getTotalOutingTime(employeeId);
//        for (Employee outingTime : totalOutingTime) {
//        	System.out.println("Controller Total Outing Time -> "+ outingTime.getTotalOutingTime());
//            System.out.println("------------------------------");
//        }
////        employeeDetails.addAll(totalOutingTime);
//        
//        for (EmployeeDetails detail : employeeDetails) {
//            System.out.println("Employee Name: " + detail.getEmployeeName());
//            System.out.println("Date: " + detail.getDate());
//            System.out.println("Total Minutes: " + detail.getTotalTime());
//            System.out.println("Total Minutes: " + detail.getTotalTime());
//            System.out.println("Out Count: " + detail.getOutCount());
//            System.out.println("------------------------------");
//        }
//        model.addAttribute("employeeId",employeeId);
//        model.addAttribute("employeeDetails", employeeDetails);
////        model.addAttribute("totalOutingTime", totalOutingTime);
//        return "employee-details";
//    }
    
    @GetMapping("/getEmployeesDatewise/{employeeId}")
    public String getEmployeeDetails(
    		@PathVariable Long employeeId,
            Model model) {
        List<EmployeeDetails> employeeDetails = employeeDAO.getEmployeeDetailsByCurrentMonth(employeeId);
        model.addAttribute("employeeDetails", employeeDetails);
        return "employee-Details"; // Name of the view template
    }
    
    @GetMapping("/getAllEmployeesAttendance")
    public String getAllEmployeesForCurrentDate(Model model) {
        List<EmployeeDetails> employeesAttendance = employeeDAO.getAllEmployeesDataForCurrentDate();
		model.addAttribute("employeesAttendance", employeesAttendance);
        return "employees-attendance";
    }
    
    @GetMapping("/getAllEmployeeForDropDown")
    public List<Employee> getAllEmployeesForDropDown(Model model) {
        List<Employee> employees = employeeDAO.getAllEmployeesForDropDown();
        return employees;
    }
    @GetMapping("/getEmployeesDetailscustomize")
    public String getEmployeeDetails(@RequestParam Long employeeId,
                                                    @RequestParam String startDate,
                                                    @RequestParam String endDate,Model model) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        System.out.println("Employee -> "+employeeId);
        List<EmployeeDetails> employees = employeeDAO.getEmployeeDetails(employeeId, start, end);
        model.addAttribute("employees",employees);
        return "employee-details-datewise";
    }
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExcel() throws IOException {
        List<Employee> employees = employeeDAO.getAllEmployees();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");

        // Header Row
        String[] header = {"ID", "Name", "Email", "Designation"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < header.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(header[i]);
        }

        // Data Rows
        int rowIndex = 1;
        for (Employee employee : employees) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(employee.getEmployeeId());
            row.createCell(1).setCellValue(employee.getEmployeeName());
            row.createCell(2).setCellValue(employee.getEmailId());
            row.createCell(3).setCellValue(employee.getDesignation());
        }

        workbook.write(out);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.xlsx");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .headers(headers)
                .body(out.toByteArray());
    }
    
    @GetMapping("/details/download")
    public ResponseEntity<byte[]> downloadDetailsExcel() throws IOException {
        List<EmployeeDetails> employeeDetails = employeeDAO.getAllEmployeesDataForCurrentDate();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employee Details");

        // Create a date cell style
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

        // Header Row
        String[] header = {"Seriel No", "Employee Name", "Date", "Outing Total Time", "Outing Count"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < header.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(header[i]);
        }

        // Data Rows
        int rowIndex = 1;
        int serialNumber = 1; // Initialize the serial number
        for (EmployeeDetails detail : employeeDetails) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(serialNumber++); // Set and increment the serial number
            row.createCell(1).setCellValue(detail.getEmployeeName());
            
            // Set date with date format
            Cell dateCell = row.createCell(2);
            dateCell.setCellValue(java.sql.Date.valueOf(detail.getDate()));
            dateCell.setCellStyle(dateCellStyle);
            
            row.createCell(3).setCellValue(detail.getTotalTime());
            row.createCell(4).setCellValue(detail.getOutCount());
        }

        workbook.write(out);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_details.xlsx");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .headers(headers)
                .body(out.toByteArray());
    }
    
    @GetMapping("/details/download/{employeeId}")
    public ResponseEntity<byte[]> downloadMonthlyAttendanceDetails(@PathVariable Long employeeId,
            Model model) throws IOException {
    	
        List<EmployeeDetails> employeeDetails = employeeDAO.getEmployeeDetailsByCurrentMonth(employeeId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employee Monthly Attendance Details");

        // Create a date cell style
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

        // Header Row
        String[] header = {"Seriel No","Employee Name", "Date", "Outing Total Time", "Outing Count"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < header.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(header[i]);
        }

        // Data Rows
        int rowIndex = 1;
        int serialNumber = 1; // Initialize the serial number
        for (EmployeeDetails detail : employeeDetails) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(serialNumber++); // Set and increment the serial number
            row.createCell(1).setCellValue(detail.getEmployeeName());
            
            // Set date with date format
            Cell dateCell = row.createCell(2);
            dateCell.setCellValue(java.sql.Date.valueOf(detail.getDate()));
            dateCell.setCellStyle(dateCellStyle);
            
            row.createCell(3).setCellValue(detail.getTotalTime());
            row.createCell(4).setCellValue(detail.getOutCount());
        }

        workbook.write(out);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_details.xlsx");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .headers(headers)
                .body(out.toByteArray());
    }
    
    @GetMapping("/downloadDateWise/{employeeId}")
    public ResponseEntity<byte[]> downloadAttendanceDetailsDateWise(@PathVariable Long employeeId,
            @RequestParam String startDate,
            @RequestParam String endDate,Model model) throws IOException {
    	
    	LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<EmployeeDetails> employeeDetails = employeeDAO.getEmployeeDetails(employeeId, start, end);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employee Monthly Attendance Details");

        // Create a date cell style
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

        // Header Row
        String[] header = {"Seriel No","Employee Name", "Date", "Outing Total Time", "Outing Count"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < header.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(header[i]);
        }

        // Data Rows
        int rowIndex = 1;
        int serialNumber = 1; // Initialize the serial number
        for (EmployeeDetails detail : employeeDetails) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(serialNumber++); // Set and increment the serial number
            row.createCell(1).setCellValue(detail.getEmployeeName());
            
            // Set date with date format
            Cell dateCell = row.createCell(2);
            dateCell.setCellValue(java.sql.Date.valueOf(detail.getDate()));
            dateCell.setCellStyle(dateCellStyle);
            
            row.createCell(3).setCellValue(detail.getTotalTime());
            row.createCell(4).setCellValue(detail.getOutCount());
        }

        workbook.write(out);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_details.xlsx");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .headers(headers)
                .body(out.toByteArray());
    }
}