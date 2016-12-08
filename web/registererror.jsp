<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Welcome to iBei</title>
</head>
<body>
<h1 align="center">Register</h1>
<table align="center" border="0" width="10%" cellpadding="5">
    <tbody>
    <h4 align="center">Username or Password incorrect</h4>
    <s:form method="post" action="register" >
        <tr>
            <td> <s:textfield name="username" label="username"/></td>
        </tr>
        <tr>
            <td> <s:password name="password" label="password"/></td>
        </tr>
        <tr>
            <td></td>
            <td align="right"><s:submit value="REGISTER"/> </td>
        </tr>
    </s:form>
    <tr>
        <td></td>
        <td align="right"><s:a action="login"><button>BACK</button></s:a></td>
    </tr>
    </tbody>
</table>
</body>
