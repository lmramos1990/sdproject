<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>iBei - Display Auction Results</title>
    <script type="text/javascript" src="bootstrap/js/websocket.js"></script>
</head>
<body>
    <c:out value="auction id: ${sessionScope.detailauctionbean.detailAuctionObject.auctionid}"/><br>
    <c:out value="auction title: ${sessionScope.detailauctionbean.detailAuctionObject.title}"/><br>
    <c:out value="auction description: ${sessionScope.detailauctionbean.detailAuctionObject.description}"/><br>
    <c:out value="auction deadline: ${sessionScope.detailauctionbean.detailAuctionObject.deadline}"/><br>
    <c:forEach items="${sessionScope.detailauctionbean.detailAuctionObject.messages}" var="messages">
        <c:out value="message user: ${messages.username}"/><br>
        <c:out value="message text: ${messages.text}"/><br>
    </c:forEach>
    <c:forEach items="${sessionScope.detailauctionbean.detailAuctionObject.bids}" var="bids">
        <c:out value="bid user: ${bids.username}"/><br>
        <c:out value="bid amount: ${bids.amount}"/><br>
    </c:forEach>
    <s:url action="login" var="url"/>
    <s:a href="%{url}">Home</s:a>

    <div>
        <c:out value="${sessionScope.lowestprice}"/><br>
    </div>

    <div id="container">
        <div id="notifications"></div>
    </div>
</body>
</html>
