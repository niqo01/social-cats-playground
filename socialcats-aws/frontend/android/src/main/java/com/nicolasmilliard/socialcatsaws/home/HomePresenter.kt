package com.nicolasmilliard.socialcatsaws.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicolasmilliard.socialcatsaws.ActivityResultFlow
import com.nicolasmilliard.socialcatsaws.auth.Auth
import com.nicolasmilliard.socialcatsaws.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomePresenter @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val activityResultFlow: ActivityResultFlow,
  private val auth: Auth
) : ViewModel() {
  private val _models = MutableStateFlow(Model())
  val models: StateFlow<Model> get() = _models

  private val _events = Channel<Event>(RENDEZVOUS)
  val events: (Event) -> Unit get() = { _events.offer(it) }

  // Hilt does not allow for Assisted injection
  private var launcher: Launcher? = null

  init {
    checkSignInState()
    viewModelScope.launch {
      _events.consumeEach {
        when (it) {
          is Event.OnProfileClicked -> launcher!!.launchProfile(it.id)
          Event.OnSignInClicked -> launcher!!.signIn()
          Event.OnSignOutClicked -> onSignOutClicked()
          Event.ClearRefreshStatus -> TODO()
          Event.Retry -> TODO()
        }
      }
    }
    viewModelScope.launch {
      activityResultFlow.events.collect {
        auth.onActivityResult(it.requestCode, it.resultCode, it.data)
      }
    }
  }

  private fun checkSignInState() {
    viewModelScope.launch {
      auth.authStates.collect {
        _models.value =
          Model(isLoading = false, isSignedIn = it is AuthState.SignedIn)
      }
    }
  }

  fun onSignOutClicked() {
    _models.value = _models.value.copy(isLoading = true)
    viewModelScope.launch {
      auth.signOut()
      _models.value = _models.value.copy(isLoading = false)
    }
  }

  fun setLauncher(launcher: Launcher) {
    this.launcher = launcher
  }

  sealed class Event {
    data class OnProfileClicked(val id: String) : Event()
    object OnSignInClicked : Event()
    object OnSignOutClicked : Event()
    object ClearRefreshStatus : Event()
    object Retry : Event()
  }

  data class Model(
    val isLoading: Boolean = true,
    val isSignedIn: Boolean = false
  )

  interface Launcher {
    fun signIn(): Boolean
    fun launchProfile(id: String): Boolean
  }
}
