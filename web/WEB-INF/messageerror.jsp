<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>iBei - Message</title>
</head>
<body>
    <h1>Message</h1>
    <s:form action="message" method="POST" name="messageform">
        <s:textfield name="id" label="ID"/><br>
        <s:textfield name="text" label="Text"/><br>
        <s:submit value="SUBMIT"/><br>
    </s:form>
    <h4>There's not any auction with that id</h4>
</body>
</html>