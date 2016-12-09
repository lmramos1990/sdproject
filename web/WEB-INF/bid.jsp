<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>iBei - Bid</title>
</head>
<body>
    <h1>Bid</h1>
    <s:form action="bid" method="POST" name="bidform">
        <s:textfield name="id" label="Auction ID"/><br>
        <s:textfield name="amount" label="Amount"/><br>
        <s:submit value="Bid"/><br>
    </s:form>
</body>
</html>
