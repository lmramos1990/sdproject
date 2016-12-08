<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Welcome to iBei</title>
</head>
    <body>
        <h1>Log-in</h1>
        <s:form action="login" method="POST" name="loginform">
            <s:textfield name="username" label="Username"/><br>
            <s:textfield name="password" label="Password"/><br>
            <s:submit value="Log-in"/><br>
        </s:form>
    </body>
</html>