<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head>
    <title>Log Hours - Big Bad Monolith</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .header { background-color: #333; color: white; padding: 20px; text-align: center; }
        .nav { background-color: #666; padding: 10px; }
        .nav a { color: white; text-decoration: none; margin-right: 20px; }
        .nav a:hover { text-decoration: underline; }
        .content { background: white; padding: 20px; margin: 20px 0; border: 1px solid #ddd; border-radius: 5px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
        .form-group input, .form-group select, .form-group textarea {
            width: 300px; padding: 8px; border: 1px solid #ccc; border-radius: 3px;
        }
        .form-group textarea { height: 80px; resize: vertical; }
        .btn { background: #28a745; color: white; padding: 10px 15px; border: none; border-radius: 3px; cursor: pointer; }
        .btn:hover { background: #218838; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #f2f2f2; }
        .message { padding: 10px; margin: 10px 0; border-radius: 3px; }
        .success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Log Billable Hours</h1>
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
        <c:if test="${not empty message}">
            <div class="message ${messageError ? 'error' : 'success'}">
                <c:out value="${message}"/>
            </div>
        </c:if>

        <h2>Log New Hours</h2>
        <form method="post" action="<c:url value='/hours'/>">
            <input type="hidden" name="action" value="log">

            <div class="form-group">
                <label for="customerId">Customer:</label>
                <select id="customerId" name="customerId" required>
                    <option value="">Select Customer</option>
                    <c:forEach var="customer" items="${customers}">
                        <option value="${customer.id}"><c:out value="${customer.name}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label for="userId">User:</label>
                <select id="userId" name="userId" required>
                    <option value="">Select User</option>
                    <c:forEach var="user" items="${users}">
                        <option value="${user.id}"><c:out value="${user.name}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label for="categoryId">Billing Category:</label>
                <select id="categoryId" name="categoryId" required>
                    <option value="">Select Category</option>
                    <c:forEach var="category" items="${categories}">
                        <option value="${category.id}"><c:out value="${category.name}"/> ($<fmt:formatNumber value="${category.hourlyRate}" maxFractionDigits="2"/>/hr)</option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label for="hours">Hours Worked:</label>
                <input type="number" id="hours" name="hours" step="0.25" min="0" max="24" required>
            </div>

            <div class="form-group">
                <label for="date">Date:</label>
                <input type="date" id="date" name="date" value="${today}">
            </div>

            <div class="form-group">
                <label for="note">Work Description:</label>
                <textarea id="note" name="note" placeholder="Describe the work performed..."></textarea>
            </div>

            <button type="submit" class="btn">Log Hours</button>
        </form>

        <h2>Recent Hours</h2>
        <table>
            <thead>
                <tr>
                    <th>Date</th>
                    <th>Customer</th>
                    <th>User</th>
                    <th>Category</th>
                    <th>Hours</th>
                    <th>Rate</th>
                    <th>Total</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
                <c:if test="${not empty loadError}">
                    <tr><td colspan="8"><c:out value="${loadError}"/></td></tr>
                </c:if>
                <c:forEach var="hour" items="${recentHours}">
                    <tr>
                        <td><c:out value="${hour.dateLogged}"/></td>
                        <td><c:out value="${hour.customerName}"/></td>
                        <td><c:out value="${hour.userName}"/></td>
                        <td><c:out value="${hour.categoryName}"/></td>
                        <td><c:out value="${hour.hours}"/></td>
                        <td>$<fmt:formatNumber value="${hour.hourlyRate}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td>$<fmt:formatNumber value="${hour.lineTotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td><c:out value="${hour.note}"/></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>


    </div>
</body>
</html>
