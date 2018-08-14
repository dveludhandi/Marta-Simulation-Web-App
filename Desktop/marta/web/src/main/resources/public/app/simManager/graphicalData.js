var stops = [];
var routes = [];
var vehicles = [];
var scaler = 42;
var offset = 10;

function createStops(data) {
    for (var i = 0; i < data.length; i++) {
        var cur = JSON.parse(data[i]);
        stops.push(cur);
    }
}
function createRoutes(data) {
    for (var i = 0; i < data.length; i++) {
        var cur = JSON.parse(data[i]);
        routes.push(cur);
        definePoints(i);
    }
    createGraph();
}

function createVehicles(data) {
    for (var i = 0; i < data.length; i++) {
        var cur = JSON.parse(data[i]);
        vehicles.push(cur);
        sendMessage("busInfo", String(cur.id));
        var route = routes[parseInt(cur.routeId)];
        var variation = getRandomArbitrary(0, 10);
        cur["variation"] = variation;
        var pos = [route.points[cur.location][0] - (15 + (2 * i)), route.points[cur.location][1] - (10 + (2 * i))];
        cur["icon"] = svg.append("svg:image")
            .attr('width', 30)
            .attr('height', 20)
            .attr('class', "btn")
            .attr('busID', String(cur.id))
            .attr("href", "http://localhost:7000/img/busIcon.svg")
            .attr("transform", "translate(" + pos + ")");
    }
}



function getVehicle(busID){
    for (var i =0; i < vehicles.length; i++) {
        if (parseInt(vehicles[i].id) == busID) {
            return vehicles[i];
        }
    }
    return undefined;
}

var svg;
function createGraph() {
    svg = d3.select("#displayContainer").append("svg")
        .attr("width", 500)
        .attr("height", 500);
    // $("svg").css({display: "block", "margin-left": "auto", "margin-right" : "auto"});

    svg.append("rect")
        .attr("width", "100%")
        .attr("height", "100%")
        .attr("fill", "#ebebeb");
    for (var i = routes.length - 1; i >= 0; i--) {
        drawRoute(i);
    }

}
function scaleLocation(x, y) {
    return ([offset + scaler * x, offset + scaler * y])
}


function definePoints(routeId) {
    var stopIds = routes[routeId].stops;
    routes[routeId]["points"] = [];
    for (var i = 0; i < stopIds.length; i++) {
        var id = parseInt(stopIds[i]);
        var stop = stops[id];
        var stopX = parseFloat(stop.latitude);
        var stopY = parseFloat(stop.longitude);
        routes[routeId]["points"].push(scaleLocation(70 * stopX, 70 * stopY));
    }

}


function drawRoute(id) {
    var colors = ["red", "blue", "yellow", "green", "purple"];

    routes[id]["path"] = svg.append("path")
        .attr("stroke", colors[id])
        .attr("stroke-width", 2)
        .attr("stroke-opacity", 0.5)
        .attr("fill", "none")
        .data([routes[id].points])
        .attr("d", d3.svg.line()
            .tension(0) // Catmull–Rom
            .interpolate("linear-closed"));

    svg.selectAll(".point")
        .data(routes[id].points)
        .enter().append("svg:image")
        .attr('width', 20)
        .attr('height', 24)
        .attr("href", "http://localhost:7000/img/busStopIcon.png")
        .attr("transform", function(d) {
            d = [d[0] - 10, d[1] - 20];
            return "translate(" + d  + ")";
        });

}

function transition() {

    if (vehicles === undefined || vehicles.length < 1 || vehicles[1].distToNext === undefined){
        // console.log("Waiting");
        setTimeout(transition, 100);
    } else {
        // console.log("Not Waiting");
        // console.log(vehicles[1].distToNext);
        for (var i = 0; i < vehicles.length; i++) {
            var vehicle = vehicles[i];
            vehicle["icon"].transition()
                .duration(vehicle.timeToNext * 60 * 1000)
                .attrTween("transform", translateAlong(vehicle))
                .each("end", transition);
        }
        sendMessage("busInfo", String(vehicle.id));
    }
}
function getRandomArbitrary(min, max) {
    return Math.random() * (max - min) + min;
}

function createFakePathLength(vehicle) {
    lastStop = stops[vehicle.lastStop];
    nextStop = stops[vehicle.nextStop];
    p1 = scaleLocation(70 * lastStop.latitude, 70 * lastStop.longitude);
    p2 = scaleLocation(70 * nextStop.latitude, 70 * nextStop.longitude);
    var fakePath = svg.append("path")
        .data([[p1,p2]])
        .attr("stroke", "yellow")
        .attr("stroke-width", 2)
        .attr("stroke-opacity", 0.5)
        .attr("fill", "none")
        .attr("display", "none")
        .attr("d", d3.svg.line()
            .tension(0) // Catmull–Rom
            .interpolate("linear"));
    return fakePath;

}

function displayMoreInfo(data) {
    // var data = obj[1].data;
    // var type_ = obj[0].type;
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

function translateAlong(vehicle) {
    var fakePath = createFakePathLength(vehicle).node();
    var l = fakePath.getTotalLength();
    return function() {
        return function(t) {
            var p = fakePath.getPointAtLength(t * l);
            return "translate(" + (p.x - (15 + vehicle.variation)) + "," + (p.y - (10 + vehicle.variation))  + ")";
        };
    };
}
function updateVehicleInfo(obj) {
    var data = obj[1].data;
    var vehicle = getVehicle(parseInt(data.busID));

    vehicle["lastStop"] = parseInt(data.lastStop);
    vehicle["nextStop"] = parseInt(data.nextStop);
    vehicle["timeToNext"] = parseFloat(data.timeToNext);
    vehicle["distToNext"] = data.distToNext;
    vehicle["numLeaving"] = parseInt(data.numLeaving)
    vehicle["numBoarding"] = parseInt(data.numBoarding);
    vehicle["curPassengers"] = parseInt(data.curPassengers);
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
    if (obj[0].type == "stops") {
        if (stops === undefined || stops.length < 1) {
            createStops(obj[1].data);
        }
        // display stops as points

    } else if (obj[0].type == "routes") {
        if (routes === undefined || routes.length < 1) {
            createRoutes(obj[1].data);
        }
        // display routes as points

    } else if (obj[0].type == "vehicles") {
        if (vehicles === undefined || vehicles.length < 1) {
            createVehicles(obj[1].data);
        }
        // display vehicles as points
    } else if (obj[0].type == "busInfo") {
        updateVehicleInfo(obj);
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



// function waitForStops() {
//     if (stops === )
// }

function donothing() {}
window.onload = function() {
    sendMessage("data", "stops");
    // setTimeout(donothing, 5000);
    sendMessage("data", "routes");
    // setTimeout(donothing, 5000);
    sendMessage("data","vehicles");
    transition();


};
$(document).ready(function() {
    $(document).on('click', "image.btn", function () {
        var id = this.getAttribute("busID")
        sendMessage("busInfo", id);
        displayMoreInfo(getVehicle(parseInt(id)));
    });
});