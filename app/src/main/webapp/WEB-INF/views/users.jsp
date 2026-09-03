<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head>
    <title>User Management - Big Bad Monolith</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .header { background-color: #333; color: white; padding: 20px; text-align: center; }
        .nav { background-color: #666; padding: 10px; }
        .nav a { color: white; text-decoration: none; margin-right: 20px; }
        .nav a:hover { text-decoration: underline; }
        .content { background: white; padding: 20px; margin: 20px 0; border: 1px solid #ddd; border-radius: 5px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
        .form-group input { width: 300px; padding: 8px; border: 1px solid #ccc; border-radius: 3px; }
        .btn { background: #007acc; color: white; padding: 10px 15px; border: none; border-radius: 3px; cursor: pointer; }
        .btn:hover { background: #005a9e; }
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
        <h1>User Management</h1>
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

        <h2>Add New User</h2>
        <form method="post" action="<c:url value='/users'/>">
            <input type="hidden" name="action" value="add">
            <div class="form-group">
                <label for="name">User Name:</label>
                <input type="text" id="name" name="name" required>
            </div>
            <div class="form-group">
                <label for="email">Email:</label>
                <input type="email" id="email" name="email" required>
            </div>
            <button type="submit" class="btn">Add User</button>
        </form>

        <h2>Existing Users</h2>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Total Hours</th>
                    <th>Total Revenue</th>
                </tr>
            </thead>
            <tbody>
                <c:if test="${not empty loadError}">
                    <tr><td colspan="5"><c:out value="${loadError}"/></td></tr>
                </c:if>
                <c:forEach var="row" items="${userRevenue}">
                    <tr>
                        <td><c:out value="${row.userId}"/></td>
                        <td><c:out value="${row.userName}"/></td>
                        <td><c:out value="${row.userEmail}"/></td>
                        <td><fmt:formatNumber value="${row.totalHours}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td>$<fmt:formatNumber value="${row.totalRevenue}" minFractionDigits="2" maxFractionDigits="2"/></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>


    </div>
</body>
</html>
