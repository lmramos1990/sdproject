<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>iBei - Search Auction Results</title>
</head>
<body>
    <c:forEach items="${sessionScope.searchauctionbean.searchAuctionObjects}" var="auctions">
        <c:out value="${auctions.articlecode}"/><tr>
        <c:out value="${auctions.auctionid}"/><tr>
        <c:out value="${auctions.title}"/><br>
    </c:forEach>

    <s:url action="login" var="url"/>
    <s:a href="%{url}">Home</s:a>
</body>
</html>
