<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>Welcome to iBei</title>
</head>
<body>
    <h1>Log-in</h1>
    <s:form action="login" method="POST" name="loginform">
        <s:textfield name="username" label="Username"/><br>
        <s:textfield name="password" label="Password"/><br>
        <s:submit value="Log-in"/><br>
    </s:form>
    <h4>The username or the password are incorrect</h4>
</body>
</html>
