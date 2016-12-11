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