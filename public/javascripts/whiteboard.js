function WHITEBOARD(wsURL) {

	// master list of lines drawn
	var lines = new Array();
	
	var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
	var wbSocket = new WS(wsURL)

	// don't send any messages until the socket is open
	var send = function(msg){};
	wbSocket.onopen = function() {
		console.log("websocket is open")
		send = function(msg){ wbSocket.send(JSON.stringify(msg)) };
	}
    
	var receiveEvent = function(event) {
		var data = JSON.parse(event.data)
		lines.push(data)
		redraw()
	}

	wbSocket.onmessage = receiveEvent

	var curline;
	var canvas = document.getElementById('whiteboard');
	var context = canvas.getContext("2d");

	// make the canvas full screen
	canvas.width = $(window).width();
	canvas.height = $(window).height();
	
	function redraw(){
		canvas.width = canvas.width; // Clears the canvas
		
		for(var i=0; i < lines.length; i++)
		{
			var line = lines[i]

			context.beginPath();
			context.moveTo(line.x1, line.y1);
			context.lineTo(line.x2, line.y2);
			context.closePath();
			context.stroke();
		}
		
	}
	
	$('#whiteboard').mousedown( function(e) {
		e.preventDefault();
		var mouseX = e.pageX - this.offsetLeft;
		var mouseY = e.pageY - this.offsetTop;
		
		curline = {'x1':mouseX, 'y1':mouseY}

		return false;
	});
	
	$('#whiteboard').mousemove( function(e) {
		e.preventDefault();
		var mouseX = e.pageX - this.offsetLeft;
		var mouseY = e.pageY - this.offsetTop;

		if (curline) {
			curline.x2 = mouseX
			curline.y2 = mouseY
			send(curline)
			curline = {'x1':mouseX, 'y1':mouseY}
		}
	});
	
	$('#whiteboard').mouseup(function(e){
		e.preventDefault();
		var mouseX = e.pageX - this.offsetLeft;
		var mouseY = e.pageY - this.offsetTop;
		
		if (curline) {
			curline.x2 = mouseX
			curline.y2 = mouseY
			send(curline)
			curline = undefined
		}
	});

	// touch events
	canvas.addEventListener('touchstart', function(e) {
		e.preventDefault();
		var touch = e.touches[0];

		curline = {'x1':touch.pageX, 'y1':touch.pageY}
	}, false);

	canvas.addEventListener('touchmove', function(e) {
		e.preventDefault();
		var touch = e.touches[0];

		if (curline) {
			curline.x2 = touch.pageX
			curline.y2 = touch.pageY
			send(curline)
			curline = {'x1':touch.pageX, 'y1':touch.pageY}
		}
	}, false);

	canvas.addEventListener('touchend', function(e) {
		e.preventDefault();
		var touch = e.touches[0];

		if (curline) {
			curline.x2 = touch.pageX
			curline.y2 = touch.pageY
			send(curline)
			curline = undefined
		}
	}, false);

}
