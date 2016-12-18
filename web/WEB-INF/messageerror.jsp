<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>iBei - Message</title>
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

<div id="contact-form" class="align">
    <div>
        <h4>POST A MESSAGE</h4>
    </div>

    <form method="POST" action="message.action" name="messageform">

        <div>
            <label for="name">
                <span class="required">AuctionID </span>
                <input type="text" id="name" name="name" value="" placeholder="Enter a valid id" required="required" tabindex="1" autofocus="autofocus" />
            </label>
        </div>

        <div>
            <label for="message">
                <span class="required">Message</span>
                <textarea id="message" name="message" placeholder="Please write your message here." tabindex="5" required="required"></textarea>
            </label>
        </div>
        <div>
            <button  name="submit" type="submit" id="submit" >SEND</button>
        </div>

    </form>
    <h4>There's not any auction with that id</h4>
    <div id="container">
        <div id="notifications"></div>
    </div>
</div>
</body>

</html>