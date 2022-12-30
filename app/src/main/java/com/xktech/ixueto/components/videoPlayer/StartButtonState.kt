package com.xktech.ixueto.components.videoPlayer

class StartButtonState constructor(
    _playState: PlayState = PlayState.PAUSE,
    _isFaceChecking: Boolean = false,
    _isPreFaceChecking: Boolean = false
) {
    var playState: PlayState = _playState
    var isPreFaceCheck: Boolean = _isPreFaceChecking
    set(value) {
        if(!value){
            field = value
            isFaceChecking = false
        }
    }
    var isFaceChecking: Boolean = _isFaceChecking
}