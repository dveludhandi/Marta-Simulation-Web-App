function jsonToTable(obj) {
    var data = obj[1].data;
    var type_ = obj[0].type;

    // Conditional based on type of obj i.e. bus, stop, route
    if (type_ == 'vehicles') {
        createVehicleTable(data)
    } else if (type_ == 'routes') {
        createRouteTable(data);
    } else if (type_ == 'stops'){
        createStopTable(data);
    }
}
function createVehicleTable(data) {
    // Creating table header
    var tbl = '<table><thead><tr>';
    tbl += '<th>' + 'ID' + '</th>';
    tbl += '<th>' + 'Route' + '</th>';
    tbl += '<th>' + 'Location' + '</th>';
    tbl += '<th>' + 'Riders' + '</th>';
    tbl += '<th>' + 'Capacity' + '</th>';
    tbl += '<th>' + 'Speed' + '</th>';
    tbl += '</tr></thead><tbody style = "cursor:pointer">';

    // adding table body
    for (var i = 0; i < data.length; i++) {
        tbl += '<tr>';
        var cur = JSON.parse(data[i]);
        tbl += '<td>'+cur.id+'</td>';
        tbl += '<td>'+cur.routeId+'</td>';
        tbl += '<td>'+cur.location+'</td>';
        tbl += '<td>'+cur.numberOfRiders +'</td>';
        tbl += '<td>'+cur.capacity+'</td>';
        tbl += '<td>'+cur.speed+'</td>';
        tbl += '</tr>';

    }
    tbl += '</tbody></table>';
    $('#busTable').html(tbl);
}

function createRouteTable(data) {
    // Creating table header
    var tbl = '<table><thead><tr>';
    tbl += '<th>' + 'ID' + '</th>';
    tbl += '<th>' + 'Index' + '</th>';
    tbl += '<th>' + 'Name' + '</th>';
    for (var i = 0; i < 10; i++) {
        tbl += '<th>Stop ' + i+ '</th>';
    }
    tbl += '</tr></thead><tbody style = "cursor:pointer">';

    // adding table body
    for (var i = 0; i < data.length; i++) {
        tbl += '<tr>';
        var cur = JSON.parse(data[i]);
        tbl += '<td>'+cur.id+'</td>';
        tbl += '<td>'+cur.index+'</td>';
        tbl += '<td>'+cur.name+'</td>';
        for (var j = 0; j < cur.stops.length; j++) {
            tbl += '<td>'+ cur.stops[j] +'</td>';
        }
        for (var j = cur.stops.length; j < 10; j++) {
            tbl += '<td>DNE</td>';
        }
        tbl += '</tr>';

    }
    tbl += '</tbody></table>';
    $('#routeTable').html(tbl);
}

function createStopTable(data) {
    // Creating table header
    var tbl = '<table><thead><tr>';
    tbl += '<th>' + 'ID' + '</th>';
    tbl += '<th>' + 'Name' + '</th>';
    tbl += '<th>' + 'Riders' + '</th>';
    tbl += '<th>' + 'Latitude' + '</th>';
    tbl += '<th>' + 'Longitude' + '</th>';
    tbl += '</tr></thead><tbody style = "cursor:pointer">';

    // adding table body
    for (var i = 0; i < data.length; i++) {
        tbl += '<tr>';
        var cur = JSON.parse(data[i]);
        tbl += '<td>'+cur.id+'</td>';
        tbl += '<td>'+cur.name+'</td>';
        tbl += '<td>'+cur.riders+'</td>';
        tbl += '<td>'+cur.latitude +'</td>';
        tbl += '<td>'+cur.longitude+'</td>';
        tbl += '</tr>';

    }
    tbl += '</tbody></table>';
    $('#stopTable').html(tbl);
}

function displayMoreInfo(obj) {
    var data = obj[1].data;
    var type_ = obj[0].type;
    var info = '<ul>';
    info += '<li class="list-group-item">Last Stop: ' + data.lastStop + '</li>';
    info += '<li class="list-group-item">Next Stop: ' + data.nextStop + '</li>';
    info += '<li class="list-group-item">Time Until Arrival At Next Stop: ' + data.timeToNext + ' Minute(s)</li>';
    info += '<li class="list-group-item">Distance To Next Stop: ' + data.distToNext + ' Mile(s)</li>';
    info += '<li class="list-group-item">Passengers Leaving: ' + data.numLeaving + '</li>';
    info += '<li class="list-group-item">Passengers Boarding: ' + data.numBoarding + '</li>';
    info += '<li class="list-group-item">Current Number of Passengers: ' + data.curPassengers + '</li>';
    info += '</ul>';
    $('#busInfoList').html(info);
    document.getElementById("moreBusInfo").style.display="inline"

}



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
    if (obj[0].type == 'busInfo') {
        displayMoreInfo(obj);
    } else {
        jsonToTable(obj);
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



//Loads information from all csv files into respective tables
window.onload = function() {
    // loading data from simulation into tables
    sendMessage("data","vehicles");
    sendMessage("data", "routes");
    sendMessage("data", "stops");
};

// Shows/Hides selected tables
$(document).ready(function() {
    $('#busTable').on('click', 'tbody tr', function () {
        for (var i = 3; i <= document.getElementById('busTable').rows[0].cells.length; i++) {
            $('#busTable tr > *:nth-child(' + i + ')').show();
        }
        var elem = $(this);
        elem.siblings().hide();
        var busID = elem.find('td').eq(0).html();
        sendMessage("busInfo", busID);
        $('#busTable').width('80%');
    });

    $('#routeTable').on('click', 'tbody tr', function () {
        for (var i = 3; i <= document.getElementById('routeTable').rows[0].cells.length; i++) {
            $('#routeTable tr > *:nth-child(' + i + ')').show();
        }
        var elem = $(this);
        elem.siblings().hide();
        $('#routeTable').width('80%');
    });

    $('#stopTable').on('click', 'tbody tr', function () {
        for (var i = 3; i <= document.getElementById('stopTable').rows[0].cells.length; i++) {
            $('#stopTable tr > *:nth-child(' + i + ')').show();
        }
        var elem = $(this);
        elem.siblings().hide();
        $('#stopTable').width('80%');

    });

    $('#busLink').click(function() {


        document.getElementById("routeInfo").style.display = "none";
        document.getElementById("stopInfo").style.display = "none";
        document.getElementById("moreBusInfo").style.display = "none";
        $('#busTable').width('30%');

        if (document.getElementById("busInfo").style.display == "none") {
            for (var i = 3; i <= document.getElementById('busTable').rows[0].cells.length; i++) {
                $('#busTable tr > *:nth-child(' + i + ')').hide();
            }
            $('#busTable tr').show();
            document.getElementById("busInfo").style.display="inline";
        }
        else {
            $('#busInfo').toggle();
        }
    });
    $('#routeLink').click(function() {
        document.getElementById("busInfo").style.display = "none";
        document.getElementById("stopInfo").style.display = "none";
        document.getElementById("moreBusInfo").style.display = "none";

        $('#routeTable').width('30%');
        if (document.getElementById("routeInfo").style.display == "none") {
            for (var i = 3; i <= document.getElementById('routeTable').rows[0].cells.length; i++) {
                $('#routeTable tr > *:nth-child(' + i + ')').hide();
            }
            $('#routeTable tr').show();
            document.getElementById("routeInfo").style.display="inline";

        }
        else {
            $('#routeInfo').toggle();
        }
    });
    $('#stopLink').click(function() {
        document.getElementById("busInfo").style.display = "none";
        document.getElementById("routeInfo").style.display = "none";
        document.getElementById("moreBusInfo").style.display = "none";

        $('#stopTable').width('30%');
        if (document.getElementById("stopInfo").style.display == "none") {
            for (var i = 3; i <= document.getElementById('stopTable').rows[0].cells.length; i++) {
                $('#stopTable tr > *:nth-child(' + i + ')').hide();
            }
            $('#stopTable tr').show();
            document.getElementById("stopInfo").style.display="inline";
        }
        else {
            $('#stopInfo').toggle();
        }
    });
});

