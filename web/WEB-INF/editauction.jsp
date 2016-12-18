<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>iBei - Create Auction</title>
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
        <h4>EDIT AN AUCTION</h4>
    </div>
    <br>
    <form method="POST" action="editauction.action" name="editauctionform">

        <div>
            <label for="article" style="width: 350px;">
                <span class="required">Auction ID </span>
                <input type="text" id="auction" name="bid" value="" placeholder="Enter a valid id" required="required" tabindex="1" autofocus="autofocus" />
            </label>
        </div>

        <div>
            <label for="article" style="width: 350px;">
                <span class="required">Article Code </span>
                <input type="text" id="article" name="bid" value="" placeholder="Enter a valid code"  tabindex="1" autofocus="autofocus" />
            </label>
        </div>


        <div>
            <label for="title" class="required" style="width: 350px;">
                <span class="required"> Title</span>
                <input type="text" id="title" name="title" value="" placeholder="Please Enter a title" tabindex="1" autofocus="autofocus" />
            </label>
        </div>
        <div>
            <label for="deadline" style="width: 350px;">
                <span class="required">Description </span>
                <input type="text" id="deadline" name="bid" value="" placeholder="Please Enter a Description"  tabindex="1" autofocus="autofocus" />
            </label>
        </div>
        <div>
            <label for="description" style="width: 350px;">
                <span class="required">Deadline </span>
                <input type="text" id="description" name="bid" value="" placeholder="Enter a valid Deadline"  tabindex="1" autofocus="autofocus" />
            </label>
        </div>
        <div>
            <label for="amount" style="width: 350px;">
                <span class="required">Amount </span>
                <input type="text" id="amount" name="bid" value="" placeholder="Enter a valid Amount"  tabindex="1" autofocus="autofocus" />
            </label>
        </div>
        <div>
            <button  name="submit" type="submit" id="submit" >EDIT</button>
        </div>

    </form>
    <div id="container">
        <div id="notifications"></div>
    </div>

</div>
</body>

</html>


