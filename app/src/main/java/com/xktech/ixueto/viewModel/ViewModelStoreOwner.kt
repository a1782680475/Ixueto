package com.xktech.ixueto.viewModel

import androidx.lifecycle.*

private val vMStores = HashMap<String, VMStore>()

fun LifecycleOwner.injectViewModel() {
    this::class.java.declaredFields.forEach { field ->
        field.getAnnotation(VMScope::class.java)?.also { scope ->
            val element = scope.scopeName
            var store: VMStore
            if (vMStores.keys.contains(element)) {
                store = vMStores[element]!!
            } else {
                store = VMStore()
                vMStores[element] = store
            }
            store.register(this)
            val clazz = field.type as Class<ViewModel>
            val vm = ViewModelProvider(store, ViewModelProvider.NewInstanceFactory()).get(clazz)
            field.set(this, vm)
        }
    }
}

class VMStore : ViewModelStoreOwner {

    private val bindTargets = ArrayList<LifecycleOwner>()
    private var vmStore: ViewModelStore? = null

    fun register(host: LifecycleOwner) {
        if (!bindTargets.contains(host)) {
            bindTargets.add(host)
            host.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        host.lifecycle.removeObserver(this)
                        bindTargets.remove(host)
                        if (bindTargets.isEmpty()) {
                            vMStores.entries.find { it.value == this@VMStore }?.also {
                                vmStore?.clear()
                                vMStores.remove(it.key)
                            }
                        }
                    }
                }
            })
        }
    }

    override fun getViewModelStore(): ViewModelStore {
        if (vmStore == null)
            vmStore = ViewModelStore()
        return vmStore!!
    }
}