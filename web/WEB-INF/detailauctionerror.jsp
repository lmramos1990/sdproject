<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>iBei - Detail Auction</title>
    </head>
    <body>
    <h1>Detail Auction</h1>
    <h4>There's not any auction with that id</h4>
    <s:form action="detailauction" method="POST" name="detailauctionform">
        <s:textfield name="detail" label="ID"/><br>
        <s:submit value="SUBMIT"/><br>
    </s:form>
    <s:url action="login" var="url"/>
    <s:a href="%{url}">Home</s:a>
    </body>
</html>
