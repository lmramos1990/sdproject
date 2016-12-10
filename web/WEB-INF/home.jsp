<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>iBei</title>
</head>
<body>
    <h1>this is the web-site landing page</h1>
    <s:url action="bid" var="url"/>
    <s:a href="%{url}">BID</s:a>
    <s:url action="createauction" var="url"/>
    <s:a href="%{url}">CREATE AUCTION</s:a>
    <s:url action="editauction" var="url"/>
    <s:a href="%{url}">EDIT AUCTION</s:a>
    <s:url action="searchauction" var="url"/>
    <s:a href="%{url}">SEARCH AUCTION</s:a>
    <s:url action="detailauction" var="url"/>
    <s:a href="%{url}">DETAIL AUCTION</s:a>
    <s:url action="myauctions" var="url"/>
    <s:a href="%{url}">MY AUCTIONS</s:a>
    <s:url action="message" var="url"/>
    <s:a href="%{url}">MESSAGE</s:a>
    <s:url action="onlineusers" var="url"/>
    <s:a href="%{url}">ONLINE USERS</s:a>


</body>
</html>
