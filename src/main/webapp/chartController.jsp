<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Line Chart</title>
    <!-- Load Google Charts -->
    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
    <script type="text/javascript">
        google.charts.load('current', {'packages':['corechart']});
        google.charts.setOnLoadCallback(drawChart);

        function drawChart() {
            var jsonData = $.ajax({
                url: "/data/chartData", // Define URL to fetch data from your Spring controller
                dataType: "json",
                async: false
            }).responseText;

            var data = new google.visualization.DataTable(jsonData);

            var options = {
                title: 'Mobile App Usage',
                curveType: 'function',
                legend: { position: 'bottom' },
                backgroundColor: { fill:'transparent' },
                colors: ['#FF5722'], // Change color here
                hAxis: {
                    title: 'Month'
                },
                vAxis: {
                    title: 'Usage'
                }
            };

            var chart = new google.visualization.LineChart(document.getElementById('line_chart'));

            chart.draw(data, options);
        }
    </script>
</head>
<body>
    <div id="line_chart" style="width: 900px; height: 500px"></div>
</body>
</html>
