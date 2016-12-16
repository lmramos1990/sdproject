<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>iBei - Online Users</title>
    <script type="text/javascript" src="bootstrap/js/websocket.js"></script>
</head>
<body>
    <s:url action="login" var="url"/>
    <s:a href="%{url}">Home</s:a>

    <div id="container">
        <div id="notifications"></div>
    </div>
</body>
</html>
