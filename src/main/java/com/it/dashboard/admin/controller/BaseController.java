package com.it.dashboard.admin.controller;

import java.text.NumberFormat;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.it.dashboard.smc.dao.EstimateDao;

@RequestMapping("/welcome")
@Controller
public class BaseController {

    @Autowired
    private EstimateDao estimateDao;

    @RequestMapping("/")
    public ModelAndView welcome() throws JsonProcessingException {
        ModelAndView model = new ModelAndView("admin/index");

        // Client (account not opened)
        int newClientsCont = estimateDao.getNewClientsCount("DISCO1", "MGT");

        // Sales
        int PendingSolarEatimatesCount = estimateDao.PendingSolarEatimatesCountByDivision("DISCO1", "MGT");

        // Pending estimate count
        int count = estimateDao.pendingEstimateCountByDivision("DISCO1", "MGT");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(true);

        // Retrieve data for the chart
        LocalDate endDate = LocalDate.now().minusDays(1); // Yesterday
        LocalDate startDate = endDate.minusDays(6); // 6 days ago

        List<Object[]> loanApplicationsChartData = estimateDao.getDataForLoanApplications("DISCO1",
                startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        List<String> daysOfWeek = getLast7Days();

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = "[]";
        String jsonDaysOfWeek = "[]";
        try {
            jsonData = objectMapper.writeValueAsString(loanApplicationsChartData);
            jsonDaysOfWeek = objectMapper.writeValueAsString(daysOfWeek);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Add JSON data to the model
        model.addObject("salesData", jsonData);
        model.addObject("daysOfWeek", jsonDaysOfWeek);

        // Pending estimate count
        model.addObject("pendEstCount", nf.format(count));
        model.addObject("menu", "1");

        // New clients count
        model.addObject("newClientsCont", nf.format(newClientsCont));

        // Sales count
        model.addObject("PendingSolarEatimatesCount", nf.format(PendingSolarEatimatesCount));

        return model;
    }

    // Method to get dates for the last 7 days excluding today
    private List<String> getLast7Days() {
        List<String> days = new ArrayList<String>();

        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Calculate the start and end of the 7-day period
        LocalDate startDate = currentDate.minusDays(7); // Subtract 7 days to go back 6 days, excluding today
        LocalDate endDate = currentDate.minusDays(1); // Subtract 1 day for yesterday

        // Iterate through the days of the 7-day period
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate currentDay = startDate;
        while (!currentDay.isAfter(endDate)) {
            days.add(currentDay.format(formatter));
            currentDay = currentDay.plusDays(1);
        }

        return days;
    }
}
