<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>iBei - My Auctions</title>
    <script type="text/javascript" src="bootstrap/js/websocket.js"></script>
</head>
<body>
<body>
    <c:forEach items="${sessionScope.myauctionsbean.myAuctionsList}" var="myauctions">
        <c:out value="${myauctions.auctionid}"/><tr>
        <c:out value="${myauctions.articlecode}"/><tr>
        <c:out value="${myauctions.title}"/><br>
    </c:forEach>

    <s:url action="login" var="url"/>
    <s:a href="%{url}">Home</s:a>

    <div id="container">
        <div id="notifications"></div>
    </div>
</body>
</html>
