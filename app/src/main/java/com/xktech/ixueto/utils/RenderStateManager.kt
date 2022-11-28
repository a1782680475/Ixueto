package com.xktech.ixueto.utils

class RenderStateManager {
    private val renderStateMap = mutableMapOf<String, Boolean>()
    private var onAllRendered: (() -> Unit)? = null
    fun register(name: String) {
        renderStateMap[name] = false
    }

    fun rendered(name: String) {
        if(renderStateMap[name]!=null) {
            renderStateMap[name] = true
            if (renderedCheck()) {
                onAllRendered?.let { it() }
            }
        }
    }

    private fun renderedCheck(): Boolean {
        for (renderStatePair in renderStateMap) {
            if (!renderStatePair.value) {
                return false
            }
        }
        return true
    }

    fun setOnAllRenderedListener(listener: () -> Unit) {
        onAllRendered = listener
    }
}