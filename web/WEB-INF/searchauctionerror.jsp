<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Welcome to iBei</title>
    </head>
    <body>
    <h1>Search Auction</h1>
    <h4>There's not any auction with that code</h4>
    <s:form action="searchauction" method="POST" name="searchauctionform">
        <s:textfield name="code" label="Code"/><br>
        <s:submit value="SEARCH"/><br>
    </s:form>
    </body>
</html>