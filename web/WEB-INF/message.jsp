<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="">
    <meta name="author" content="">

    <title>iBei - Message</title>
    <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet">

    <link href="bootstrap/styles/dashboard.css" rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-inverse navbar-fixed-top">
        <div class="container-fluid">
            <div class="navbar-header">
                <s:url action="login" var="url"/>
                <a class="navbar-brand" href="${url}">iBei</a>
            </div>
        </div>
    </nav>
    <div class="container-fluid">
        <div class="row">
            <div class="col-sm-3 col-md-2 sidebar">
                <ul class="nav nav-sidebar">
                    <li><s:url action="createauction" var="url"/>
                        <s:a href="%{url}">Create Auction</s:a></li>
                    <li><s:url action="editauction" var="url"/>
                        <s:a href="%{url}">Edit Auction</s:a></li>
                    <li><s:url action="searchauction" var="url"/>
                        <s:a href="%{url}">Search Auction</s:a></li>
                    <li><s:url action="detailauction" var="url"/>
                        <s:a href="%{url}">Detail Auction</s:a></li>
                    <li><s:url action="myauctions" var="url"/>
                        <s:a href="%{url}">My Auctions</s:a></li>
                    <li><s:url action="bid" var="url"/>
                        <s:a href="%{url}">Bid</s:a></li>
                    <li class="active"><s:url action="message" var="url"/>
                        <s:a href="%{url}">Message</s:a></li>
                    <li><s:url action="onlineusers" var="url"/>
                        <s:a href="%{url}">Online Users</s:a></li>
                    <li><s:url action="associatewithfacebook" var="url"/>
                        <s:a href="%{url}">Associate with Facebook</s:a></li>
                    <li><s:url action="logout" var="url"/>
                        <s:a href="%{url}">Logout</s:a></li>
                </ul>
            </div>
            <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
                <form action="message.action" method="post" >
                    <div>
                        <input name="auctionid" align="center" type="text" placeholder="Auction ID" required="required" autofocus>
                    </div>
                    <div>
                        <input name="text" align="center" type="text" placeholder="Text" required="required" autofocus>
                    </div>
                    <div>
                        <input type="submit" value="Send Message">
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
    <script>window.jQuery || document.write('<script src="../../assets/js/vendor/jquery.min.js"><\/script>')</script>
    <script src="bootstrap/js/bootstrap.min.js"></script>
</body>
</html>

