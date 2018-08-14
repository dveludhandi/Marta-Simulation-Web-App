// Connecting to server socket
console.log("Attempting to connect");
var connection = new WebSocket('ws://127.0.0.1:4444');

connection.onopen = function () {
    console.log('Connected!');
};
// Log errors
connection.onerror = function (error) {
    console.log('WebSocket Error ' + error);
};
// Log messages from the server
connection.onmessage = function (msg) {
    var obj = JSON.parse(msg.data);
    if (obj[0].type == 'isInit') {
        determineDisplay(obj);
    } else if (obj[0].type == "alterState") {
    }
};

function sendMessage(label, msg){
    // Wait until the state of the socket is not ready and send the message when it is...
    waitForSocketConnection(connection, function(){
        connection.send(JSON.stringify({
            "label":label,
            "msg":msg
        }));
    });
}

// Make the socket messages wait until the connection is made...
function waitForSocketConnection(socket, callback){
    setTimeout(
        function () {
            if (socket.readyState === 1) {
                if(callback != null){
                    callback();
                }
                return;

            } else {
                waitForSocketConnection(socket, callback);
            }

        }, 5); // wait 5 milisecond for the connection...
}

function determineDisplay(obj) {
    var initialized = obj[1].data.isInit;
    if (initialized) {
        document.getElementById("controlOptions").style.display="inline";
    } else {
        document.getElementById("simStartOptions").style.display="inline";
    }
}

window.onload = function () {
    sendMessage("isInit", "none");
};

$(document).ready(function() {
    $('#loadNew').click(function () {
        sendMessage("initialize","startNew");
        document.getElementById("simStartOptions").style.display="none";
        document.getElementById("controlOptions").style.display="inline";
    });
    $('#resumePast').click(function () {
        sendMessage("initialize","resume");
        document.getElementById("simStartOptions").style.display="none";
        document.getElementById("controlOptions").style.display="inline";

    });

});
