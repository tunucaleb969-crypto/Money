package com.kwame.money

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * InputMethodService is not an Activity or Fragment, so Android does not give it
 * a LifecycleOwner, ViewModelStoreOwner, or SavedStateRegistryOwner automatically.
 * Jetpack Compose requires all three to be attached to a view before it can render
 * anything inside an IME. This class provides minimal, correct implementations of
 * all three and a helper to attach them to the keyboard's root view.
 *
 * This is a known, documented pattern for using Compose outside of Activities
 * (used by several open-source "Compose keyboard" projects). If we hit lifecycle-
 * related crashes on first build, this file is the first place to check.
 */
class KeyboardLifecycleOwner :
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    /** Call once, right after creating the keyboard's root view. */
    fun attachToView(view: View) {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
    }

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }
}
