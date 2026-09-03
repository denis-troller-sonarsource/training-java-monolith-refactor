<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head>
    <title>Reports - Big Bad Monolith</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .header { background-color: #333; color: white; padding: 20px; text-align: center; }
        .nav { background-color: #666; padding: 10px; }
        .nav a { color: white; text-decoration: none; margin-right: 20px; }
        .nav a:hover { text-decoration: underline; }
        .content { background: white; padding: 20px; margin: 20px 0; border: 1px solid #ddd; border-radius: 5px; }
        .form-group { margin-bottom: 15px; display: inline-block; margin-right: 20px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
        .form-group select, .form-group input { padding: 8px; border: 1px solid #ccc; border-radius: 3px; }
        .btn { background: #007acc; color: white; padding: 10px 15px; border: none; border-radius: 3px; cursor: pointer; }
        .btn:hover { background: #005a9e; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #f2f2f2; }
        .report-section { margin: 30px 0; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }
        .summary-box { background: #e7f3ff; padding: 15px; margin: 15px 0; border-radius: 5px; }
        .text-right { text-align: right; }
        .text-center { text-align: center; }
        .error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; padding: 10px; border-radius: 3px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Billing Reports</h1>
    </div>

    <div class="nav">
        <a href="<c:url value='/dashboard'/>">Dashboard</a>
        <a href="<c:url value='/customers'/>">Customers</a>
        <a href="<c:url value='/users'/>">Users</a>
        <a href="<c:url value='/categories'/>">Billing Categories</a>
        <a href="<c:url value='/hours'/>">Log Hours</a>
        <a href="<c:url value='/reports'/>">Reports</a>
    </div>

    <div class="content">
        <h2>Generate Reports</h2>
        <form method="get" action="<c:url value='/reports'/>">
            <div class="form-group">
                <label for="reportType">Report Type:</label>
                <select id="reportType" name="reportType">
                    <option value="">Select Report Type</option>
                    <option value="customer" ${reportType == 'customer' ? 'selected' : ''}>Customer Bill</option>
                    <option value="monthly" ${reportType == 'monthly' ? 'selected' : ''}>Monthly Summary</option>
                    <option value="revenue" ${reportType == 'revenue' ? 'selected' : ''}>Revenue Summary</option>
                </select>
            </div>

            <c:if test="${reportType == 'customer'}">
            <div class="form-group">
                <label for="customerId">Customer:</label>
                <select id="customerId" name="customerId">
                    <option value="">Select Customer</option>
                    <c:forEach var="customer" items="${customers}">
                        <option value="${customer.id}" ${selectedCustomerId == customer.id.toString() ? 'selected' : ''}><c:out value="${customer.name}"/></option>
                    </c:forEach>
                </select>
            </div>
            </c:if>

            <c:if test="${reportType == 'monthly'}">
            <div class="form-group">
                <label for="year">Year:</label>
                <input type="number" id="year" name="year" value="${not empty selectedYear ? selectedYear : '2024'}" min="2020" max="2030">
            </div>
            <div class="form-group">
                <label for="month">Month:</label>
                <select id="month" name="month">
                    <option value="01" ${selectedMonth == '01' ? 'selected' : ''}>January</option>
                    <option value="02" ${selectedMonth == '02' ? 'selected' : ''}>February</option>
                    <option value="03" ${selectedMonth == '03' ? 'selected' : ''}>March</option>
                    <option value="04" ${selectedMonth == '04' ? 'selected' : ''}>April</option>
                    <option value="05" ${selectedMonth == '05' ? 'selected' : ''}>May</option>
                    <option value="06" ${selectedMonth == '06' ? 'selected' : ''}>June</option>
                    <option value="07" ${selectedMonth == '07' ? 'selected' : ''}>July</option>
                    <option value="08" ${selectedMonth == '08' ? 'selected' : ''}>August</option>
                    <option value="09" ${selectedMonth == '09' ? 'selected' : ''}>September</option>
                    <option value="10" ${selectedMonth == '10' ? 'selected' : ''}>October</option>
                    <option value="11" ${selectedMonth == '11' ? 'selected' : ''}>November</option>
                    <option value="12" ${selectedMonth == '12' ? 'selected' : ''}>December</option>
                </select>
            </div>
            </c:if>

            <button type="submit" class="btn">Generate Report</button>
        </form>

        <c:if test="${not empty reportError}">
            <div class="error"><c:out value="${reportError}"/></div>
        </c:if>

        <c:if test="${reportType == 'customer' and not empty customerBill}">
        <div class="report-section">
            <h2>Customer Bill Report</h2>
            <div class="summary-box">
                <h3>Bill To:</h3>
                <p><strong><c:out value="${customerBill.customerName}"/></strong><br>
                Email: <c:out value="${customerBill.customerEmail}"/></p>
            </div>

            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>User</th>
                        <th>Category</th>
                        <th>Hours</th>
                        <th>Rate</th>
                        <th>Amount</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="line" items="${customerBill.lines}">
                        <tr>
                            <td><c:out value="${line.dateLogged}"/></td>
                            <td><c:out value="${line.userName}"/></td>
                            <td><c:out value="${line.categoryName}"/></td>
                            <td class="text-right"><fmt:formatNumber value="${line.hours}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td class="text-right">$<fmt:formatNumber value="${line.hourlyRate}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td class="text-right">$<fmt:formatNumber value="${line.lineTotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td><c:out value="${line.note}"/></td>
                        </tr>
                    </c:forEach>
                    <tr style="background-color: #f8f9fa; font-weight: bold;">
                        <td colspan="3">TOTAL</td>
                        <td class="text-right"><fmt:formatNumber value="${customerBill.totalHours}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td></td>
                        <td class="text-right">$<fmt:formatNumber value="${customerBill.totalAmount}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td></td>
                    </tr>
                </tbody>
            </table>
        </div>
        </c:if>

        <c:if test="${reportType == 'monthly' and not empty monthlyRows}">
        <div class="report-section">
            <h2>Monthly Summary - <c:out value="${selectedMonth}"/>/<c:out value="${selectedYear}"/></h2>
            <table>
                <thead>
                    <tr>
                        <th>Customer</th>
                        <th>Total Hours</th>
                        <th>Total Revenue</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="row" items="${monthlyRows}">
                        <tr>
                            <td><c:out value="${row.customerName}"/></td>
                            <td class="text-right"><fmt:formatNumber value="${row.totalHours}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td class="text-right">$<fmt:formatNumber value="${row.totalAmount}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        </tr>
                    </c:forEach>
                    <tr style="background-color: #f8f9fa; font-weight: bold;">
                        <td>MONTHLY TOTAL</td>
                        <td class="text-right"><fmt:formatNumber value="${monthlyTotalHours}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td class="text-right">$<fmt:formatNumber value="${monthlyTotalAmount}" minFractionDigits="2" maxFractionDigits="2"/></td>
                    </tr>
                </tbody>
            </table>
        </div>
        </c:if>

        <c:if test="${reportType == 'revenue'}">
        <div class="report-section">
            <h2>Revenue Summary</h2>

            <h3>By Customer</h3>
            <table>
                <thead>
                    <tr>
                        <th>Customer</th>
                        <th>Total Hours</th>
                        <th>Total Revenue</th>
                        <th>Average Rate</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="row" items="${revenueByCustomer}">
                        <tr>
                            <td><c:out value="${row.customerName}"/></td>
                            <td class="text-right"><fmt:formatNumber value="${row.totalHours}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td class="text-right">$<fmt:formatNumber value="${row.totalRevenue}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td class="text-right">$<fmt:formatNumber value="${row.averageRate}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <h3>By Category</h3>
            <table>
                <thead>
                    <tr>
                        <th>Category</th>
                        <th>Hourly Rate</th>
                        <th>Total Hours</th>
                        <th>Total Revenue</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="row" items="${revenueByCategory}">
                        <tr>
                            <td><c:out value="${row.categoryName}"/></td>
                            <td class="text-right">$<fmt:formatNumber value="${row.hourlyRate}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td class="text-right"><fmt:formatNumber value="${row.totalHours}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td class="text-right">$<fmt:formatNumber value="${row.totalRevenue}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        </c:if>


    </div>
</body>
</html>
