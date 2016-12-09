<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>iBei - Edit Auction</title>
</head>
<body>
    <h1>Edit Auction</h1>
    <s:form action="editauction" method="POST" name="editauctionform">
        <s:textfield name="articlecode" label="Article Code"/>
        <s:textfield name="title" label="Title"/>
        <s:textfield name="description" label="Description"/>
        <s:textfield name="deadline" label="Deadline"/>
        <s:textfield name="amount" label="Amount"/>
        <s:submit value="Edit Auction"/><br>
    </s:form>
</body>
</html>
