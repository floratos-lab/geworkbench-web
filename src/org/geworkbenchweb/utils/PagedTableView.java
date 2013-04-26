package org.geworkbenchweb.utils;

import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.addon.tableexport.ExcelExport;
 

public class PagedTableView extends PagedTable{
	
	private static final long serialVersionUID = 4201018486738162112L;
	
 
	
	public PagedTableView()
	{
		 setSizeFull();
		 setImmediate(true);
	}
	 
	 
	
	public void csvExport(String fileName)
	{
		CsvExport csvExport = new CsvExport(this);
		csvExport.excludeCollapsedColumns();
		csvExport.setExportFileName(fileName);
		csvExport.setDisplayTotals(false);
		csvExport.export();
	}
	
	public void excelExport(String fileName)
	{
		
		 
		ExcelExport excelExport = new CsvExport(this);
		excelExport.excludeCollapsedColumns();
		excelExport.setExportFileName(fileName);
		excelExport.setDisplayTotals(false);
		excelExport.export();
	}
	

}
