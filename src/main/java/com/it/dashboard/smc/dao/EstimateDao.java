package com.it.dashboard.smc.dao;

import java.util.List;

//Chart imports
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.it.dashboard.master.domain.TableRowValue;


public interface EstimateDao {
	public int pendingEstimateCountByDivision(String division, String appName);

	List<TableRowValue> pendingEstimateDetailByDivision(String division,
			String appName);

	List<TableRowValue> pendingEstimateDetailByProvince(String province,
			String appName);
	
	  // Updated method declaration to Clients(account not opened)
    public int getNewClientsCount(String division, String appName);
    
    
    
    //Chart
    //List<Integer> getWeeklyApplicationCounts(String division, String appName);
 // Method for retrieving chart data
    List<Object[]> getDataForLoanApplications(String appName, String startDate, String endDate);



    // Updated method declaration to include parameters
    public int PendingSolarEatimatesCountByDivision(String division, String appName);
   
	
}

