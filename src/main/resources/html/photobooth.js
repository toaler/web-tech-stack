// Main initialization
document.addEventListener('DOMContentLoaded', function () {

    // Global variables
    var video = document.querySelector('video');
    var audio, audioType;

    // Get the video stream from the camera with getUserMedia
    navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia ||
        navigator.mozGetUserMedia || navigator.msGetUserMedia;

    window.URL = window.URL || window.webkitURL || window.mozURL || window.msURL;
    if (navigator.getUserMedia) {

        // Evoke getUserMedia function
        navigator.getUserMedia({video: true, audio: true}, onSuccessCallback, onErrorCallback);

        function onSuccessCallback(stream) {
            // Use the stream from the camera as the source of the video element
            video.src = window.URL.createObjectURL(stream) || stream;

            // Autoplay
            video.play();

            // HTML5 Audio
            audio = new Audio();
            audioType = getAudioType(audio);
            if (audioType) {
                audio.src = 'polaroid.' + audioType;
                audio.play();
            }
        }

        // Display an error
        function onErrorCallback(e) {
            var expl = 'An error occurred: [Reason: ' + e.code + ']';
            console.error(expl);
            alert(expl);
            return;
        }
    } else {
        document.querySelector('.container').style.visibility = 'hidden';
        document.querySelector('.warning').style.visibility = 'visible';
        return;
    }

    // The canPlayType() function doesn�t return true or false. In recognition of how complex
    // formats are, the function returns a string: 'probably', 'maybe' or an empty string.
    function getAudioType(element) {
        if (element.canPlayType) {
            if (element.canPlayType('audio/mp4; codecs="mp4a.40.5"') !== '') {
                return ('aac');
            } else if (element.canPlayType('audio/ogg; codecs="vorbis"') !== '') {
                return ("ogg");
            }
        }
        return false;
    }

    var canvas = document.getElementById("canvas");
    var ctx = canvas.getContext('2d');


    ctx.drawImage(video, 0, 0, 320, 240);

    timer = setInterval(
        function () {
            ctx.drawImage(video, 0, 0, 320, 240);
        }, 25);

}, false);

//timer = setInterval(
//            function () {
//                ctx.drawImage(video, 0, 0, 320, 240);
//                var data = canvas.get()[0].toDataURL('image/jpeg', 1.0);
//                newblob = dataURItoBlob(data);
//                ws.send(newblob);
//            }, 250);
//    }


function dataURItoBlob(dataURI) {
    // convert base64/URLEncoded data component to raw binary data held in a string
    var byteString;
    if (dataURI.split(',')[0].indexOf('base64') >= 0)
        byteString = atob(dataURI.split(',')[1]);
    else
        byteString = unescape(dataURI.split(',')[1]);

    // separate out the mime component
    var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];

    // write the bytes of the string to a typed array
    var ia = new Uint8Array(byteString.length);
    for (var i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
    }

    return new Blob([ia], {type: mimeString});
}


//var ws = new WebSocket("ws://localhost:8080/video");
//ws.onopen = function () {
//              console.log("Openened connection to websocket");
//}

