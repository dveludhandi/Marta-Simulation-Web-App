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
    if (obj[0].type == 'statusRequest') {
        displayStatus(obj);
    } else if (obj[0].type == "alterState") {
    } else if(obj[0].type == "nearestBusRequest") {
        loadNearestBus(obj);
    } else if (obj[0].type = "pushNext") {
        location.reload();
    }
};

var isPlaying = true;

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

function loadNearestBus(obj) {
    var data = obj[1].data;

    var info = '<ul><li class="list-group-item">ID: ';
    info += data.id + '</li><li class="list-group-item">Time until arrival: ';
    info += data.timeToNext + ' Minute(s)</li></ul>';
    $('#nextBusInfo').html(info);
}

window.onload = function () {
    sendMessage("statusRequest", "none");
    sendMessage("nearestBusRequest", "none");
};

function displayStatus(obj) {
    var data = obj[1].data;
    var info = '<ul><li id=simStatus class="list-group-item">Simulation is Currently: ';

    if (data.on == true) {
        info += "Running";
        $("#playSim").text("Pause");
    } else {
        info+= "Paused";
        $("#playSim").text("Play");
    }
    info += '</li><li class="list-group-item">Current Simulation Speed: ' + data.speed + '</li>';
    info += '</ul>';
    $('#simStatusList').html(info);

}

$(document).ready(function() {
    document.getElementById("playSim").addEventListener("click", function(){
        console.log("Clicked Simulation Button");
        if (isPlaying == true) {
            sendMessage("alterState","false");
            $("#playSim").text("Play");
            // $("#simStatus").text("Simulation is Currently: Paused");
            isPlaying = false;
        } else if (isPlaying == false) {
            sendMessage("alterState", "true");
            $("#playSim").text("Pause");
            // $("#simStatus").text("Simulation is Currently: Running");
            isPlaying = true;
        }
        sendMessage("statusRequest", "none");

    });

    $('#pushNext').click( function() {
        sendMessage("pushNext", "none");
    })
});
