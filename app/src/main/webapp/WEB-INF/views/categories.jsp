<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<html>
<head>
    <title>Billing Categories - Big Bad Monolith</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .header { background-color: #333; color: white; padding: 20px; text-align: center; }
        .nav { background-color: #666; padding: 10px; }
        .nav a { color: white; text-decoration: none; margin-right: 20px; }
        .nav a:hover { text-decoration: underline; }
        .content { background: white; padding: 20px; margin: 20px 0; border: 1px solid #ddd; border-radius: 5px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
        .form-group input, .form-group textarea { width: 300px; padding: 8px; border: 1px solid #ccc; border-radius: 3px; }
        .form-group textarea { height: 60px; resize: vertical; }
        .btn { background: #007acc; color: white; padding: 10px 15px; border: none; border-radius: 3px; cursor: pointer; }
        .btn:hover { background: #005a9e; }
        .btn-small { background: #ffc107; color: black; padding: 5px 10px; font-size: 12px; }
        .btn-small:hover { background: #e0a800; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #f2f2f2; }
        .message { padding: 10px; margin: 10px 0; border-radius: 3px; }
        .success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .inline-form { display: inline-block; margin-left: 10px; }
        .inline-form input { width: 80px; padding: 3px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Billing Categories</h1>
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

        <h2>Add New Billing Category</h2>
        <form method="post" action="<c:url value='/categories'/>">
            <input type="hidden" name="action" value="add">
            <div class="form-group">
                <label for="name">Category Name:</label>
                <input type="text" id="name" name="name" required>
            </div>
            <div class="form-group">
                <label for="description">Description:</label>
                <textarea id="description" name="description"></textarea>
            </div>
            <div class="form-group">
                <label for="hourlyRate">Hourly Rate ($):</label>
                <input type="number" id="hourlyRate" name="hourlyRate" step="0.01" min="0" required>
            </div>
            <button type="submit" class="btn">Add Category</button>
        </form>

        <h2>Existing Categories</h2>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Hourly Rate</th>
                    <th>Total Hours</th>
                    <th>Total Revenue</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:if test="${not empty loadError}">
                    <tr><td colspan="7"><c:out value="${loadError}"/></td></tr>
                </c:if>
                <c:forEach var="category" items="${categoryRows}">
                    <tr>
                        <td><c:out value="${category.id}"/></td>
                        <td><c:out value="${category.name}"/></td>
                        <td><c:out value="${category.description}"/></td>
                        <td>$<fmt:formatNumber value="${category.hourlyRate}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td><fmt:formatNumber value="${category.totalHours}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td>$<fmt:formatNumber value="${category.totalRevenue}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td>
                            <form method="post" action="<c:url value='/categories'/>" class="inline-form">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="id" value="${category.id}">
                                $<input type="number" name="newRate" step="0.01" value="${category.hourlyRate}" required>
                                <button type="submit" class="btn btn-small">Update Rate</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>


    </div>
</body>
</html>
