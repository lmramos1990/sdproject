<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>iBei - Bid</title>
    <script type="text/javascript" src="bootstrap/js/websocket.js"></script>
</head>
<body>
    <h1>Bid</h1>
    <s:form action="bid" method="POST" name="bidform">
        <s:textfield name="auctionid" label="Auction ID"/><br>
        <s:textfield name="amount" label="Amount"/><br>
        <s:submit value="BID"/><br>
    </s:form>
    <s:url action="login" var="url"/>
    <s:a href="%{url}">Home</s:a>

    <div id="container">
        <div id="notifications"></div>
    </div>
</body>
</html>
