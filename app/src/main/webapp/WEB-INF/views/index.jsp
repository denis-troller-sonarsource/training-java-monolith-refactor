<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head>
    <title>Big Bad Monolith - Time Tracker</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .header { background-color: #333; color: white; padding: 20px; text-align: center; }
        .nav { background-color: #666; padding: 10px; }
        .nav a { color: white; text-decoration: none; margin-right: 20px; }
        .nav a:hover { text-decoration: underline; }
        .dashboard { display: flex; gap: 20px; margin: 20px 0; }
        .card { background: white; border: 1px solid #ddd; padding: 20px; border-radius: 5px; flex: 1; }
        .card h3 { margin-top: 0; color: #333; }
        .number { font-size: 2em; font-weight: bold; color: #007acc; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Big Bad Monolith Time Tracker</h1>
        <p>Legacy JSP Interface - Needs Modernization!</p>
    </div>

    <div class="nav">
        <a href="<c:url value='/dashboard'/>">Dashboard</a>
        <a href="<c:url value='/customers'/>">Customers</a>
        <a href="<c:url value='/users'/>">Users</a>
        <a href="<c:url value='/categories'/>">Billing Categories</a>
        <a href="<c:url value='/hours'/>">Log Hours</a>
        <a href="<c:url value='/reports'/>">Reports</a>
    </div>

    <c:if test="${not empty errorMessage}">
        <p><c:out value="${errorMessage}"/></p>
    </c:if>

    <div class="dashboard">
        <div class="card">
            <h3>Total Customers</h3>
            <div class="number"><c:out value="${customerCount}"/></div>
        </div>
        <div class="card">
            <h3>Total Users</h3>
            <div class="number"><c:out value="${userCount}"/></div>
        </div>
        <div class="card">
            <h3>Total Revenue</h3>
            <div class="number">$<fmt:formatNumber value="${totalRevenue}" minFractionDigits="2" maxFractionDigits="2"/></div>
        </div>
    </div>

    <div style="background: white; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
        <h2>Welcome to the Legacy Time Tracker</h2>

    </div>
</body>
</html>
