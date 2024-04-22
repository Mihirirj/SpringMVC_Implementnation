package com.it.dashboard.smc.dao;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.it.dashboard.master.domain.TableRowValue;
import com.it.dashboard.util.common.EmSelector;

//Imports for the chart
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class EstimateDaoImpl extends EmSelector implements EstimateDao {

	private static final Log log = LogFactory.getLog(EstimateDaoImpl.class);

	/*
	 * @SuppressWarnings("unchecked")
	 * 
	 * @Override public int pendingEstimateCountByDivision(String division,
	 * String appName) { //returns 0 or 1 after checking the given pivno is
	 * refundable or not by status// int count = 0; try { String qrystr =
	 * "SELECT  count(a.id.applicationId) "+
	 * " from Application a  where a.status not in ('E','D') "+
	 * "and a.id.deptId   IN  (select dept_id from Gldeptm g where g.compId in "
	 * + "(select comp_id  from Glcompm c where  c.compId = 'DISCO1' or "+
	 * " c.parentId = 'DISCO1' or c.grpComp = 'DISCO1')) "+
	 * "and a.submitDate >= TO_DATE('2018/01/01','yyyy/mm/dd') and a.applicationType in ('NC') "
	 * ;
	 * 
	 * Query query = getEntityManager(appName).createQuery(qrystr);
	 * query.setParameter("division", division);
	 * 
	 * List<String> list = query.getResultList();
	 * 
	 * if (list.isEmpty()){ count = 0; } else{ count=list.size();
	 * 
	 * } } catch (Exception e) { e.printStackTrace(); return 0; }
	 * 
	 * 
	 * 
	 * return count; }
	 */

	@SuppressWarnings("unchecked")
	@Override
	public int pendingEstimateCountByDivision(String division, String appName) {
		int count = 0;
		System.out.println("division " + division);
		String qryStr = "SELECT count(application_id)  "
				+ " from applications a  where a.status not in ('E','D') "
				+ " and a.dept_id   IN  (select dept_id from gldeptm where comp_id in "
				+ " (select comp_id  from glcompm where  comp_id = :division or "
				+ " parent_id = :division or grp_comp = :division)) "
				+ " and a.submit_date >= TO_DATE('2018/01/01','yyyy/mm/dd') "
				+ "and a.application_type in ('NC') ";

		Query query = getEntityManager(appName).createNativeQuery(qryStr);
		query.setParameter("division", division);

		List<BigDecimal> list = query.getResultList();

		if (list.isEmpty()) {
			count = 0;
		} else {
			count = list.get(0).intValue();

		}
		return count;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TableRowValue> pendingEstimateDetailByDivision(String division,
			String appName) {
		List<TableRowValue> rowList = new ArrayList<TableRowValue>();
		System.out.println("division " + division);
		String qryStr = "SELECT distinct  (case  when  b.lvl_no = 60 then b.comp_id else b.parent_id  end  ) as Province, "
				+ " count(application_id) as No_of_pending_estimation "
				+ " from applications a , gldeptm a1, glcompm b where a.status not in ('E','D') "
				+ " and a.dept_id   IN  (select dept_id from gldeptm where comp_id in "
				+ " (select comp_id  from glcompm where  comp_id = :division or "
				+ "  parent_id =:division or grp_comp =:division)) "
				+ " and a.submit_date >= TO_DATE('2018/01/01','yyyy/mm/dd') and a.application_type in ('NC') "
				+ " and a1.dept_id = a.dept_id "
				+ " and  b.status =2 and a1.comp_id=b.comp_id and b.lvl_no < 90 and "
				+ " a1.status=2 "
				+ " group by case  when  b.lvl_no = 60 then b.comp_id else b.parent_id  end "
				+ " order by 1 ";

		Query query = getEntityManager(appName).createNativeQuery(qryStr);
		query.setParameter("division", division);

		List<Object[]> list = query.getResultList();
		for (int i = 0; i < list.size(); i++) {
			Object[] objArr = list.get(i);
			TableRowValue row = new TableRowValue((String) objArr[0],
					(BigDecimal) objArr[1]);
			rowList.add(row);
		}
		return rowList;

	}

	// /////////////NewClientsCount(account not opened)////////////////////

	@Override
	public int getNewClientsCount(String division, String appName) {
	    int count = 0;
	    try {
	        // Adjusted query to match the new requirements
	        String qryStr = "SELECT distinct count(a.job_no) as No_of_pending_jobs "
	                + "FROM DASHBOARD a "
	                + "WHERE a.ACCOUNT_NO is null "
	                + "AND a.JOB_FINISHED_DATE is not null "
	                + "AND a.application_type in ('NC') "
	                + "AND a.dept_id IN (SELECT dept_id FROM gldeptm WHERE status=2 AND comp_id in "
	                + "(SELECT comp_id FROM glcompm WHERE status =2 AND comp_id = :division OR parent_id = :division OR grp_comp = :division))";

	        Query query = getEntityManager(appName).createNativeQuery(qryStr);
	        query.setParameter("division", division);

	        List<BigDecimal> list = query.getResultList();

	        if (!list.isEmpty()) {
	            count = list.get(0).intValue();
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return count;
	}

	// ////////////////////
	
	/*-------------------Chart----------------*/
	
	@Override
	public List<Object[]> getDataForLoanApplications(String appName, String startDate, String endDate) {
	    List<Object[]> LoanApplicationsChartData = new ArrayList<Object[]>();
	    try {
	        String qryStr = "SELECT SUBMIT_DATE, COUNT(*) AS Num_Applications " +
	                        "FROM APPLICATIONS " +
	                        "WHERE APPLICATION_TYPE = 'NC' " +
	                        "AND APPLICATION_SUB_TYPE = 'IP' " +
	                        "AND SUBMIT_DATE >= TO_DATE(:startDate, 'YYYY-MM-DD') " +
	                        "AND SUBMIT_DATE < TO_DATE(:endDate, 'YYYY-MM-DD') " +
	                        "GROUP BY SUBMIT_DATE";

	        Query query = getEntityManager(appName).createNativeQuery(qryStr);
	        query.setParameter("startDate", startDate);
	        query.setParameter("endDate", endDate);

	        LoanApplicationsChartData = query.getResultList();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return LoanApplicationsChartData;
	}


/*---------------Chart end--------------*/
	@SuppressWarnings("unchecked")
	@Override
	public List<TableRowValue> pendingEstimateDetailByProvince(String province,
			String appName) {
		List<TableRowValue> rowList = new ArrayList<TableRowValue>();
		try {
			System.out.println("provincedao" + province + "##");
			/*
			 * String qryStr =
			 * "SELECT distinct    (select comp_nm from glcompm where comp_id= b.comp_id )as comp_nm , "
			 * +
			 * " (select dept_id ||' - '||dept_nm from gldeptm where dept_id= a.dept_id)as dept_nm, "
			 * +" count(application_id) as No_of_pending_estimation " +
			 * " from applications a , gldeptm a1, glcompm b where a.status not in ('E','D') "
			 * +
			 * " and a.dept_id   IN  (select dept_id from gldeptm where comp_id in "
			 * +" (select comp_id  from glcompm where  comp_id = :province or "
			 * +" parent_id =:province )) " +
			 * " and a.submit_date >= TO_DATE('2018/01/01','yyyy/mm/dd') and a.application_type in ('NC') "
			 * +" and a1.dept_id = a.dept_id "
			 * +" and  b.status =2 and a1.comp_id=b.comp_id and a1.status=2  "
			 * +" group by b.comp_id  ,a.dept_id " +" order by 1";
			 */

			String qryStr = " SELECT distinct    (select comp_nm from glcompm where comp_id= b.comp_id )as comp_nm , "
					+ " (select dept_id ||' - '||dept_nm from gldeptm where dept_id= a.dept_id)as dept_nm, "
					+ " count(application_id) as No_of_pending_estimation "
					+ " from applications a , gldeptm a1, glcompm b where a.status not in ('E','D') "
					+ " and a.dept_id   IN  (select dept_id from gldeptm where comp_id in "
					+ " (select comp_id  from glcompm where  trim(comp_id) = :province or "
					+ " trim(parent_id) = :province )) "
					+ " and a.submit_date >= TO_DATE('2018/01/01','yyyy/mm/dd') and a.application_type in ('NC') "
					+ " and a1.dept_id = a.dept_id "
					+ " and  b.status =2 and a1.comp_id=b.comp_id and a1.status=2  "
					+ " group by b.comp_id  ,a.dept_id " + " order by 1";

			Query query = getEntityManager(appName).createNativeQuery(qryStr);
			query.setParameter("province", province);

			List<Object[]> list = query.getResultList();
			System.out.println("list size" + list.size());
			for (int i = 0; i < list.size(); i++) {
				Object[] objArr = list.get(i);
				TableRowValue row = new TableRowValue((String) objArr[0],
						(String) objArr[1], (BigDecimal) objArr[2]);
				rowList.add(row);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rowList;

	}

	@Override
	public int PendingSolarEatimatesCountByDivision(String division, String appName) {
	    int count = 0;
	    try {
	        String qryStr = "SELECT count(application_id) " +
	                        "FROM applications a " +
	                        "WHERE a.status NOT IN ('E', 'D') " +
	                        "AND a.dept_id IN (SELECT dept_id FROM gldeptm WHERE comp_id IN " +
	                        "(SELECT comp_id FROM glcompm WHERE comp_id = :division OR parent_id = :division OR grp_comp = :division)) " +
	                        "AND a.submit_date >= TO_DATE('2018/01/01','yyyy/mm/dd') " +
	                        "AND a.application_type IN ('NC')";

	        Query query = getEntityManager(appName).createNativeQuery(qryStr);
	        query.setParameter("division", division);

	        List<BigDecimal> list = query.getResultList();

	        if (!list.isEmpty()) {
	            count = list.get(0).intValue();
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return count;
	}

	
	
	
	
}
