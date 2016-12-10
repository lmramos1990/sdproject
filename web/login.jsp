<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Welcome to iBei</title>

    <script type="text/javascript">
        var websocket = null;

        window.onload = function() {
            connect('ws://' + window.location.host + '/ws');
        }

        function connect(host) {
            if ('WebSocket' in window)
                websocket = new WebSocket(host);
            else if ('MozWebSocket' in window)
                websocket = new MozWebSocket(host);
            else {
                writeToHistory('Get a real browser which supports WebSocket.');
                return;
            }

            websocket.onopen = onOpen;
            websocket.onclose = onClose;
            websocket.onmessage = onMessage;
            websocket.onerror = onError;
        }

        function onOpen(event) {

        }

        function onClose(event) {

        }

        function onMessage(message) {

        }

        function onError(event) {

        }
    </script>

</head>
    <body>
        <h1>Login</h1>
        <s:form action="login" method="POST" name="loginform">
            <s:textfield name="username" label="Username"/><br>
            <s:textfield name="password" type="password" label="Password"/><br>
            <s:submit value="LOGIN"/><br>
        </s:form>
    </body>
</html>