<%--
  Created by IntelliJ IDEA.
  User: lmramos
  Date: 12/8/16
  Time: 4:26 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
