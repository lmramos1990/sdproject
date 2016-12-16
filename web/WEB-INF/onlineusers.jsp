<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>iBei - Online Users</title>
    <script type="text/javascript" src="bootstrap/js/websocket.js"></script>
</head>
<body>
    <c:forEach items="${sessionScope.onlinebean.onlineUsersList}" var="someone">
        <c:out value="${someone.username}"/><br>
    </c:forEach>
    <s:url action="login" var="url"/>
    <s:a href="%{url}">Home</s:a>

    <div id="container">
        <div id="notifications"></div>
    </div>
</body>
</html>
