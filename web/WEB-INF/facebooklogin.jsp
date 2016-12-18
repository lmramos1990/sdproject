<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>iBei - Facebook Credentials</title>
    <script type="text/javascript" src="bootstrap/js/websocket.js"></script>
</head>
<body>
    <s:form action="facebooklogin" method="POST" name="facebooklogin">
        <s:textfield name="email" label="Email"/><br>
        <s:textfield type="password" name="password" label="Password"/><br>
        <s:submit value="Login"/>
    </s:form>

    <s:url action="login" var="url"/>
    <s:a href="%{url}">Home</s:a>

    <div id="container">
        <div id="notifications"></div>
    </div>
</body>
</html>
