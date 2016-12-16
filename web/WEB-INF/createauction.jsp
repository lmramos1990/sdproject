<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>iBei - Create Auction</title>
    <script type="text/javascript" src="bootstrap/js/websocket.js"></script>
</head>
    <body>
        <h1>Create Auction</h1>
        <s:form action="createauction" method="POST" name="createauctionform">
            <s:textfield name="articlecode" label="Article Code"/><br>
            <s:textfield name="title" label="Title"/><br>
            <s:textfield name="description" label="Description"/><br>
            <s:textfield name="deadline" label="Deadline"/><br>
            <s:textfield name="amount" label="Amount"/><br>
            <s:submit value="Create Auction"/><br>
        </s:form>
        <s:url action="login" var="url"/>
        <s:a href="%{url}">Home</s:a>

        <div id="container">
            <div id="notifications"></div>
        </div>
    </body>
</html>
