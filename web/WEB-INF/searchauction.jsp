<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>iBei - Search Auction</title>
    <script type="text/javascript" src="bootstrap/js/websocket.js"></script>
</head>
<body>
    <h1>Search Auction</h1>
    <s:form action="searchauction" method="POST" name="searchauctionform">
        <s:textfield name="articlecode" label="Code"/><br>
        <s:submit value="SEARCH"/><br>
    </s:form>
    <s:url action="login" var="url"/>
    <s:a href="%{url}">Home</s:a>

    <div id="container">
        <div id="notifications"></div>
    </div>
</body>
</html>