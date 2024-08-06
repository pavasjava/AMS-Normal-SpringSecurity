package com.ams.util;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ams.model.Employee;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class EmployeeDetailsExelView extends AbstractXlsxView{
	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		//download file name
		response.addHeader("Content-Disposition", "attachment:filename=Employee.xlsx");
		//reading data from controller
		@SuppressWarnings("unchecked")
		List<Employee> list=(List<Employee>)model.get("employee");
		//create new sheet
		Sheet sheet=workbook.createSheet("EMPLOYEE DATA");	
		setHead(sheet);
		setBody(sheet,list);
	}
	
	public void setHead(Sheet sheet) {
		Row row=sheet.createRow(0);
		
		//create cell
		row.createCell(0).setCellValue("EMPLOYEE ID");
		row.createCell(1).setCellValue("EMPLOYEE NAME");
		row.createCell(2).setCellValue("DATE");
		row.createCell(3).setCellValue("IN TIME");	
		row.createCell(4).setCellValue("OUT TIME");
	}
	
	private void setBody(Sheet sheet,List<Employee> list) {
		int rownum=1;
		for(Employee employee:list) {
			Row row=sheet.createRow(rownum++);
			row.createCell(0).setCellValue(employee.getEmployeeId());
			row.createCell(1).setCellValue(employee.getEmployeeName());
			row.createCell(2).setCellValue(employee.getDate());
			row.createCell(3).setCellValue(employee.getInTime());
			row.createCell(4).setCellValue(employee.getOutTime());
//			row.createCell(4).setCellValue(employee.get);
		}	
	}
}
