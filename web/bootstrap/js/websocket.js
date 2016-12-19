var websocket = null;

window.onload = function() {
    connect('ws://' + window.location.host + '/ws');
};

function connect(host) {
    if('WebSocket' in window) websocket = new WebSocket(host);
    else return;

    // set the event listeners below
    websocket.onopen = onOpen;
    websocket.onclose = onClose;
    websocket.onmessage = onMessage;
    websocket.onerror = onError;
}

function onOpen(event) {
    console.log(event);
}

function onClose(event) {
    console.log(event);
    console.log("[WEBSOCKET] GOING TO CLOSE")
}

function onMessage(message) {
    writeNotification(message.data);
}

function onError(event) {
    console.log(event);
    console.log("SOME ERROR OCURRED");
}

function writeNotification(message) {
    var notification = document.getElementById('notifications');
    var line = document.createElement('p');
    line.style.wordWrap = 'break-word';
    line.innerHTML = message;
    notification.appendChild(line);
    notification.scrollTop = notification.scrollHeight;
}