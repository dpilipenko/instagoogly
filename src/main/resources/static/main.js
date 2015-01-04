var stompClient = null;
var output = document.querySelector('#output');
var outputCtx = output.getContext('2d');

connect();

function connect() {
	var socket = new SockJS('/cameraIn');
	stompClient = Stomp.over(socket);
	stompClient.connect({}, function(frame){
		stompClient.subscribe('/topic/cameraOut', function(res) {
			readImageURI(JSON.parse(res.body).base64Image);
		});
		doVideoCapture();
	});
}

function sendImageURI(uri) {
	stompClient.send('/app/cameraIn', {}, JSON.stringify({'base64Image': uri}));
}

function readImageURI(uri) {
	var image = new Image();
	image.src = uri;
	image.onload = function() {
		outputCtx.drawImage(image, 0, 0);
	};
}

function doVideoCapture() {
	/* Call if user accepts to allow video */
	function doVideoAccepted(stream) {
		var video = document.querySelector('#video');
		video.src = window.URL.createObjectURL(stream);
		
		var canvasOld = document.querySelector('#canvas');
		var ctx = canvasOld.getContext('2d');
		setInterval(function () {
			ctx.drawImage(video, 0, 0, output.width, output.height);
			sendImageURI(canvasOld.toDataURL('image/jpeg', 1.0));
		}, 100);
	}
	
	/* Call if user declines to allow video */
	function doVideoDeclined(e) {
		alert('video declined');
	}
	
	/* Call if browser does not support video */
	function doVideoAbsent() {
		alert('Your browser does not support web cameras');
	}
	
	navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia ||
							navigator.mozGetUserMedia || navigator.msGetUserMedia ||
							navigator.oGetUserMedia;
	if (navigator.getUserMedia) {
		navigator.getUserMedia({video: true}, doVideoAccepted, doVideoDeclined);
	} else {
		doVideoAbsent();
	}
}