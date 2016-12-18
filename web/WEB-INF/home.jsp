<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>iBei - Home</title>

    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap.min.css" />
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap.min.css" />
    <link rel="stylesheet" href="bootstrap/css/styles/signin.css">

    <script type="text/javascript" src="js/jquery-1.10.2.min.js"></script>
    <script type="text/javascript" src="bootstrap/js/bootstrap.min.js"></script>

    <!-- you need to include the shieldui css and js assets in order for the charts to work -->
    <link rel="stylesheet" type="text/css" href="http://www.shieldui.com/shared/components/latest/css/light-bootstrap/all.min.css" />
    <link id="gridcss" rel="stylesheet" type="text/css" href="http://www.shieldui.com/shared/components/latest/css/dark-bootstrap/all.min.css" />

    <script type="text/javascript" src="http://www.shieldui.com/shared/components/latest/js/shieldui-all.min.js"></script>
    <script type="text/javascript" src="http://www.prepbootstrap.com/Content/js/gridData.js"></script>
</head>

<body class="align">
<div id="wrapper">
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
        <div class="collapse navbar-collapse navbar-ex1-collapse ">
            <ul id="active" class="nav navbar-nav side-nav">
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

    <div id="page-wrapper">
        <div >
            <div class="col-lg-12">

                <h1 class="align">
                    <c:out value="Welcome to iBei,  ${username}."/><br><br>
                </h1>

            </div>
        </div>

        </div>
    </div>
</div>

</body>
</html>
