<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>iBei - Register</title>
</head>
<body>
    <h1>Register</h1>
    <s:form method="post" action="register" name="registerform">
        <s:textfield name="username" label="username"/><br>
        <s:password name="password" label="password"/><br>
        <s:submit value="REGISTER"/>
    </s:form>
    <h4>Error</h4>
    <s:url action="login" var="url"/>
    <s:a href="%{url}">Login</s:a>
</body>
</html>