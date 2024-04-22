package com.it.dashboard.admin.controller;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.it.dashboard.admin.domain.Sauserm;
import com.it.dashboard.admin.repo.SecurityDao;
import com.it.dashboard.issue.repo.PivDao;
import com.it.dashboard.issue.repo.PivDetailDao;
import com.it.dashboard.refund.repo.RefundDao;



@Controller

public class LoginController {

	@Autowired
	private SecurityDao securityDao;

	// to call new piv count(pr)
	@Autowired
	private PivDetailDao pivDetailDao;

	@Autowired
	private PivDao pivDao;
	
	@Autowired
	private RefundDao refundDao;
	/*
	 * @RequestMapping(value = "issue/student", method = RequestMethod.GET)
	 * public String showHelloPage(Model model) { model.addAttribute("person",
	 * new StudentModel()); return "issue/student"; }
	 * 
	 * @RequestMapping(value = "issue/contact", method = RequestMethod.GET)
	 * public ModelAndView get1() {
	 * 
	 * ContactForm1 contactForm = new ContactForm1();
	 * contactForm.setContacts(getAcctList1());
	 * 
	 * return new ModelAndView("issue/add_contact_piv" , "contactForm",
	 * contactForm); }
	 * 
	 * @RequestMapping(value = "issue/contact1", method = RequestMethod.GET)
	 * public ModelAndView get() { ContactForm contactForm = new ContactForm();
	 * 
	 * Contact c1 = new Contact(); c1.setFirstname("ss"); c1.setLastname("dd");
	 * c1.setEmail("d@d"); c1.setPhone("111");
	 * 
	 * Contact c2 = new Contact(); c2.setFirstname("ss"); c2.setLastname("dd");
	 * c2.setEmail("d@d"); c2.setPhone("111");
	 * 
	 * List<Contact> contacts = new ArrayList<Contact>(); contacts.add(c1);
	 * contacts.add(c2);
	 * 
	 * contactForm.setContacts(contacts);
	 * 
	 * return new ModelAndView("issue/add_contact" , "contactForm",
	 * contactForm); }
	 */
    
	
	
	@ExceptionHandler(NullPointerException.class)
	public String handleNullException(NullPointerException ex) {

		ModelAndView model = new ModelAndView("admin/index");
		model.addObject("errMsg", "this is null Exception.class");
		
		return "redirect:../admin/index";
	} 
	
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "admin/login", method = RequestMethod.POST)
	
	public ModelAndView login(@RequestParam Map<String,String> requestParams,HttpServletRequest request) throws Exception{
    	String userName=requestParams.get("loginUser");
		   String password=requestParams.get("password");
    	try{
		   
		   System.out.println("Start PIV Login "+userName);
		   boolean isValidLogin = securityDao.validateLogin(userName, password,"PIV");
		   int count=0,paidCount=0,toBeApprovedCount=0,toBeValidatedCount=0;
		   BigDecimal totalAmount=new BigDecimal(0);
		   NumberFormat nf = NumberFormat.getInstance();
		   nf.setGroupingUsed(true);
		   nf.setMaximumFractionDigits(2);
		   nf.setMinimumFractionDigits(2);
		  
		   if(isValidLogin)
		   { 
			   Sauserm sauserm = securityDao.getSauserm(userName,"PIV");
			   String userRole = sauserm.getUserLevel();
			   if(userRole!=null)
			   {
				   	request.getSession().setAttribute("loggedUser", userName.toUpperCase());
					request.getSession().setAttribute("loggedUserRole", userRole);
					request.getSession().setAttribute("deptId", sauserm.getRptUser().trim());
					request.getSession().setAttribute("headerName", "Dashboard");
				
					String dept = request.getSession().getAttribute("deptId").toString();
					//String userRole=request.getSession().getAttribute("loggedUserRole").toString(); 
					
					List<String> activities=refundDao.getAuthorizedActivity(userRole, dept,"PIV");
					request.getSession().setAttribute("userActivity", activities);
					
					// to get no of new pivs that issed today by the department(pr)
					//count = pivDao.findNewPivCount(dept,"PIV");
					count = 0;
				
					// to get no of pivs that paid today by the department(pr)
					//paidCount = pivDao.findPaidPivCount(dept,"PIV");
					paidCount = 0;
					
					//to get total revenue
					//totalAmount=pivDao.findTotalRevenue(dept,"P","PIV");
					totalAmount=new BigDecimal(0);
					
					//to get number of to be Approved pivs
					//toBeApprovedCount=pivDao.getAuthorisedPivDetailCount(userRole,dept,"V","PIV");
					toBeApprovedCount=0;
							
					//to get number of to be Validated pivs
					//toBeValidatedCount=pivDao.getAuthorisedPivDetailCount(userRole,dept,"S","PIV");
					toBeValidatedCount = 0;
					
					ModelAndView mv = new ModelAndView("admin/mainmenu");
					mv.addObject("newpivcount",Integer.toString(count));
					mv.addObject("newpaidpivcount",Integer.toString(paidCount));
					mv.addObject("totalAmount",nf.format(totalAmount));
					mv.addObject("toBeApprovedCount",toBeApprovedCount);
					mv.addObject("toBeValidatedCount",toBeValidatedCount);
					//mv.addObject("access", access);
					System.out.println("PIV Login success "+userName);
					return mv;
			   }
			   else
			   {
				   ModelAndView mv = new ModelAndView("admin/index");
				   mv.addObject("message", "User Level has not been set. Please contact IT Help Desk!");
				   return mv;
			   }
				
		   }
		   else
		   {
			   
			   //return "admin/mainmenu";
			  // return new ModelAndView("redirect:/");
			   ModelAndView mv = new ModelAndView("admin/index");
			   mv.addObject("message", "User name or password is incorrect!");
			   return mv;
		   }
    	}
    	catch(Exception e)
    	{
    		 System.out.println("PIV Login unsuccess "+userName);
    		e.printStackTrace();
    		ModelAndView mv = new ModelAndView("admin/index");
    		mv.addObject("message", "Cannot connect to DB!");
		   	return mv;
    	}
		   
		}
	
	
	
	 
	@RequestMapping(value = "admin/mainmenu", method = RequestMethod.GET)
	 public ModelAndView redirectMainMenu(HttpServletRequest request) {
		NumberFormat nf = NumberFormat.getInstance();
		   nf.setGroupingUsed(true);
		   nf.setMaximumFractionDigits(2);
		   nf.setMinimumFractionDigits(2);
		
		request.getSession().setAttribute("headerName", "Dashboard");
		
		// to get new piv count, no of new pivs(pr)
		int count=0,paidCount=0,toBeApprovedCount=0,toBeValidatedCount=0;
		 BigDecimal totalAmount=new BigDecimal(0);
		String userRole=request.getSession().getAttribute("loggedUserRole").toString(); 
		String dept = request.getSession().getAttribute("deptId").toString();
		count = pivDao.findNewPivCount(dept,"PIV");
	    ModelAndView mv = new ModelAndView("admin/mainmenu");
		mv.addObject("newpivcount",Integer.toString(count));
					
		// to get no of pivs that paid today by the department(pr)
		paidCount = pivDao.findPaidPivCount(dept,"PIV");
		mv.addObject("newpaidpivcount",Integer.toString(paidCount));
		
		//to get total revenue
		totalAmount=pivDao.findTotalRevenue(dept,"P","PIV");
		if(totalAmount!=null)
			mv.addObject("totalAmount",nf.format(totalAmount));
		
		//to get number of to be Approved pivs
		toBeApprovedCount=pivDao.getAuthorisedPivDetailCount(userRole,dept,"V","PIV");
		mv.addObject("toBeApprovedCount",toBeApprovedCount);
		
		//to get number of to be Validated pivs
		toBeValidatedCount=pivDao.getAuthorisedPivDetailCount(userRole,dept,"S","PIV");
		mv.addObject("toBeValidatedCount",toBeValidatedCount);
		
		return mv;
	} 

	@RequestMapping("issue/ajax")
	public ModelAndView helloAjaxTest() {
		return new ModelAndView("issue/ajax", "message",
				"Crunchify Spring MVC with Ajax and JQuery Demo..");
	}


	// @RequestMapping("ajax/search")
	/*
	 * public ModelAndView search() { return new
	 * ModelAndView("ajax/select-customer", "command", new Contact()); }
	 */


	
	@RequestMapping(value = "admin/index", method = RequestMethod.GET)
	 public String logout(HttpServletRequest request) {
		request.getSession().setAttribute("headerName", "Dashboard");
		return "admin/index";
	}
	
	 
	
	 
	 
	 
	

	/****************** Private Methods *********************************/

}
