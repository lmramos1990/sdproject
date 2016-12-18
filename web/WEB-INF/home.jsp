<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>iBei - Home</title>

    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap.min.css" />
    <link rel="stylesheet" type="text/css" href="font-awesome/css/font-awesome.min.css" />
    <link rel="stylesheet" type="text/css" href="css/local.css" />
    <link rel="stylesheet" type="text/css" href="bootstrap/css/styles/signin.css" />

    <script type="text/javascript" src="js/jquery-1.10.2.min.js"></script>
    <script type="text/javascript" src="bootstrap/js/bootstrap.min.js"></script>

    <!-- you need to include the shieldui css and js assets in order for the charts to work -->
    <link rel="stylesheet" type="text/css" href="http://www.shieldui.com/shared/components/latest/css/light-bootstrap/all.min.css" />
    <link id="gridcss" rel="stylesheet" type="text/css" href="http://www.shieldui.com/shared/components/latest/css/dark-bootstrap/all.min.css" />

    <script type="text/javascript" src="http://www.shieldui.com/shared/components/latest/js/shieldui-all.min.js"></script>
    <script type="text/javascript" src="http://www.prepbootstrap.com/Content/js/gridData.js"></script>
</head>

<body >
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
                <h2><smal>
                    <c:out value="Welcome to iBei,  ${username}."/><br></smal>
                </h2>
                <h1>
                    <c:out value="Welcome to iBei,  ${username}."/><br><br>
                </h1>

            </div>
        </div>

        <div class="row">

            <div class="col-md-8">
                <div class="panel panel-primary">

                    <div class="panel-heading">
                        <h3 class="panel-title"><i class="fa fa-bar-chart-o"></i> MY AUCTIONS</h3>
                    </div>
                    <div class="panel-body">
                        <div id="shieldui-chart1"></div>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title"><i class="fa fa-rss"></i>NOTIFICATIONS</h3>
                    </div>
                    <div class="panel-body feed">
                        <section class="feed-item">
                            <div class="icon pull-left">
                                <i class="fa fa-comment"></i>
                            </div>
                            <div class="feed-item-body">
                                <div class="text">
                                    <a href="#">John Doe</a> commented on <a href="#">What Makes Good Code Good</a>.
                                </div>
                                <div class="time pull-left">
                                    3 h
                                </div>
                            </div>
                        </section>
                        <section class="feed-item">
                            <div class="icon pull-left">
                                <i class="fa fa-check"></i>
                            </div>
                            <div class="feed-item-body">
                                <div class="text">
                                    <a href="#">Merge request #42</a> has been approved by <a href="#">Jessica Lori</a>.
                                </div>
                                <div class="time pull-left">
                                    10 h
                                </div>
                            </div>
                        </section>
                        <section class="feed-item">
                            <div class="icon pull-left">
                                <i class="fa fa-plus-square-o"></i>
                            </div>
                            <div class="feed-item-body">
                                <div class="text">
                                    New user <a href="#">Greg Wilson</a> registered.
                                </div>
                                <div class="time pull-left">
                                    Today
                                </div>
                            </div>
                        </section>
                        <section class="feed-item">
                            <div class="icon pull-left">
                                <i class="fa fa-bolt"></i>
                            </div>
                            <div class="feed-item-body">
                                <div class="text">
                                    Server fail level raises above normal. <a href="#">See logs</a> for details.
                                </div>
                                <div class="time pull-left">
                                    Yesterday
                                </div>
                            </div>
                        </section>
                        <section class="feed-item">
                            <div class="icon pull-left">
                                <i class="fa fa-archive"></i>
                            </div>
                            <div class="feed-item-body">
                                <div class="text">
                                    <a href="#">Database usage report</a> is ready.
                                </div>
                                <div class="time pull-left">
                                    Yesterday
                                </div>
                            </div>
                        </section>
                        <section class="feed-item">
                            <div class="icon pull-left">
                                <i class="fa fa-shopping-cart"></i>
                            </div>
                            <div class="feed-item-body">
                                <div class="text">
                                    <a href="#">Order #233985</a> needs additional processing.
                                </div>
                                <div class="time pull-left">
                                    Wednesday
                                </div>
                            </div>
                        </section>
                        <section class="feed-item">
                            <div class="icon pull-left">
                                <i class="fa fa-arrow-down"></i>
                            </div>
                            <div class="feed-item-body">
                                <div class="text">
                                    <a href="#">Load more...</a>
                                </div>
                            </div>
                        </section>
                    </div>
                </div>
            </div>
        </div>

        </div>
    </div>
</div>
<!-- /#wrapper -->

<script type="text/javascript">
    jQuery(function ($) {
        var performance = [12, 43, 34, 22, 12, 33, 4, 17, 22, 34, 54, 67],
            visits = [123, 323, 443, 32],
            traffic = [
                {
                    Source: "Direct", Amount: 323, Change: 53, Percent: 23, Target: 600
                },
                {
                    Source: "Refer", Amount: 345, Change: 34, Percent: 45, Target: 567
                },
                {
                    Source: "Social", Amount: 567, Change: 67, Percent: 23, Target: 456
                },
                {
                    Source: "Search", Amount: 234, Change: 23, Percent: 56, Target: 890
                },
                {
                    Source: "Internal", Amount: 111, Change: 78, Percent: 12, Target: 345
                }];


        $("#shieldui-chart1").shieldChart({
            theme: "dark",

            primaryHeader: {
                text: "Visitors"
            },
            exportOptions: {
                image: false,
                print: false
            },
            dataSeries: [{
                seriesType: "area",
                collectionAlias: "Q Data",
                data: performance
            }]
        });

        $("#shieldui-chart2").shieldChart({
            theme: "dark",
            primaryHeader: {
                text: "Traffic Per week"
            },
            exportOptions: {
                image: false,
                print: false
            },
            dataSeries: [{
                seriesType: "pie",
                collectionAlias: "traffic",
                data: visits
            }]
        });

        $("#shieldui-grid1").shieldGrid({
            dataSource: {
                data: traffic
            },
            sorting: {
                multiple: true
            },
            rowHover: false,
            paging: false,
            columns: [
                { field: "Source", width: "170px", title: "Source" },
                { field: "Amount", title: "Amount" },
                { field: "Percent", title: "Percent", format: "{0} %" },
                { field: "Target", title: "Target" },
            ]
        });
    });
</script>
</body>
</html>
