<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>iBei</title>

    <script type="text/javascript" src="bootstrap/js/websocket.js"></script>
</head>
<body>
    <h1 align="center">this is the web-site landing page</h1>
    <s:url action="bid" var="url"/>
    <s:a href="%{url}">BID</s:a><br>
    <s:url action="createauction" var="url"/>
    <s:a href="%{url}">CREATE AUCTION</s:a><br>
    <s:url action="editauction" var="url"/>
    <s:a href="%{url}">EDIT AUCTION</s:a><br>
    <s:url action="searchauction" var="url"/>
    <s:a href="%{url}">SEARCH AUCTION</s:a><br>
    <s:url action="detailauction" var="url"/>
    <s:a href="%{url}">DETAIL AUCTION</s:a><br>
    <s:url action="myauctions" var="url"/>
    <s:a href="%{url}">MY AUCTIONS</s:a><br>
    <s:url action="message" var="url"/>
    <s:a href="%{url}">MESSAGE</s:a><br>
    <s:url action="onlineusers" var="url"/>
    <s:a href="%{url}">ONLINE USERS</s:a><br>
    <s:url action="logout" var="url"/>
    <s:a href="%{url}">LOG OUT</s:a><br>

    <s:url action="facebooklogin" var="url"/>
    <s:a href="%{url}">ASSOCIATE WITH FACEBOOK</s:a><br>

    <div id="container">
        <div id ="startupnotifications">
            <c:forEach items="${sessionScope.notifications}" var="notifications">
                <c:out value="${notifications}"/><br>
            </c:forEach>
        </div>
        <div id="notifications">

        </div>
    </div>
</body>
</html>
