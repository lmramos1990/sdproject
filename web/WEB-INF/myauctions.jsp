<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>iBei - My Auctions</title>
    <script type="text/javascript" src="bootstrap/js/websocket.js"></script>
    <link rel="stylesheet" href="bootstrap/css/styles/forms.css">
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap.min.css" />
    <link rel="stylesheet" href="bootstrap/css/styles/signin.css">
</head>
<body class="align">
<nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-ex1-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
        </button>
        <form action="login.action">
            <input class="navbar-brand"  type="submit" value="IBei">
        </form>
    </div>
    <div class="collapse navbar-collapse navbar-ex1-collapse ulStyle">
        <ul id="active" class="nav navbar-nav side-nav  ulStyle">
            <li class="ulStyle"><s:url action="createauction" var="url"/>
                <s:a href="%{url}">CREATE AUCTION</s:a></li>
            <li><s:url action="editauction" var="url"/>
                <s:a href="%{url}">EDIT AUCTION</s:a></li>
            <li><s:url action="searchauction" var="url"/>
                <s:a href="%{url}">SEARCH AUCTION</s:a></li>
            <li><s:url action="detailauction" var="url"/>
                <s:a href="%{url}">DETAIL AUCTION</s:a></li>
            <li><s:url action="myauctions" var="url"/>
                <s:a href="%{url}">MY AUCTIONS</s:a></li>
            <li><s:url action="bid" var="url"/>
                <s:a href="%{url}">BID</s:a></li>
            <li><s:url action="message" var="url"/>
                <s:a href="%{url}">MESSAGE</s:a></li>
            <li><s:url action="onlineusers" var="url"/>
                <s:a href="%{url}">ONLINE USERS</s:a></li>

        </ul>
        <ul class="nav navbar-nav navbar-right navbar-user">
            <li><s:url action="logout" var="url"/>
                <s:a href="%{url}">LOG OUT</s:a></li>
            <li class="dropdown user-dropdown">

            </li>
            <li class="divider-vertical"></li>

        </ul>
    </div>
</nav>
    <c:forEach items="${sessionScope.myauctionsbean.myAuctionsList}" var="myauctions">
        <c:out value="${myauctions.auctionid}"/><tr>
        <c:out value="${myauctions.articlecode}"/><tr>
        <c:out value="${myauctions.title}"/><br>
    </c:forEach>

    <div id="container">
        <div id="notifications"></div>
    </div>
</body>
</html>
