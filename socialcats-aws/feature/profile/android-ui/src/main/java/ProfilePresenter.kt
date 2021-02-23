package com.nicolasmilliard.socialcatsaws.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicolasmilliard.activityresult.ActivityResultFlow
import com.nicolasmilliard.socialcatsaws.auth.Auth
import com.nicolasmilliard.socialcatsaws.auth.AuthState
import com.nicolasmilliard.socialcatsaws.imageupload.ImageUploadNav
import com.nicolasmilliard.socialcatsaws.imageupload.ImageUploadService
import com.nicolasmilliard.textresource.TextResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.seconds

@HiltViewModel
public class ProfilePresenter @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val auth: Auth,
  private val activityResultFlow: ActivityResultFlow,
  private val imageUploadNav: ImageUploadNav,
  private val imageUploadService: ImageUploadService,
) : ViewModel() {
  private val _models = MutableStateFlow(Model())
  public val models: StateFlow<Model> get() = _models

  private val _events = Channel<Event>(RENDEZVOUS)
  public val events: (Event) -> Unit get() = { _events.offer(it) }

  // Hilt does not allow for Assisted injection
  private var launcher: Launcher? = null

  private var signedUser: AuthState.SignedIn? = null

  init {
    viewModelScope.launch {
      auth.authStates.collect {
        _models.value = _models.value.copy(showUpload = it is AuthState.SignedIn)
        signedUser = if (it is AuthState.SignedIn) it else null
      }
    }

    viewModelScope.launch {
      activityResultFlow.events.collect {
        val imagePath =
          imageUploadNav.onActivityResult(it.requestCode, it.resultCode, it.data)
        if (imagePath != null) {
          events(Event.OnImageSelected(imagePath))
        }
      }
    }
    viewModelScope.launch {
      _events.consumeEach {
        when (it) {
          is Event.OnNavUp -> launcher!!.onNavUp()
          is Event.QueryChanged -> TODO()
          Event.ClearRefreshStatus -> TODO()
          Event.Retry -> TODO()
          Event.OnUpload -> launcher!!.onUpload()
          is Event.OnImageSelected -> uploadImage(it.imagePath)
        }
      }
    }
  }

  private fun uploadImage(imagePath: String) {
    imageUploadService.scheduleUpload(
      signedUser!!.userId, imagePath,
      optimizeCost = false,
      optimizeBattery = false
    )
  }

  public fun setLauncher(launcher: Launcher) {
    this.launcher = launcher
  }

  public fun loadProfile(userId: String) {
    Timber.i("This is : $this")
    viewModelScope.launch {
      _models.value = _models.value.copy(isLoading = true, userId = userId)
      delay(5.seconds)
      val imageUrl = "u/fcdedf62-82e3-49c7-ae9b-7a548d2d89dd/0ead6a00-e6f5-4b81-8712-93697e7ccdaa"
      _models.value = _models.value.copy(
        isLoading = false,
        userId = userId,
        name = TextResource.fromText("Nico"),
        userPhoto = imageUrl
      )
    }
  }

  public sealed class Event {
    public data class QueryChanged(val query: String) : Event()
    public object ClearRefreshStatus : Event()
    public object OnNavUp : Event()
    public data class OnImageSelected(val imagePath: String) : Event()
    public object OnUpload : Event()
    public object Retry : Event()
  }

  public data class Model(
    val isLoading: Boolean = true,
    val showUpload: Boolean = false,
    val userId: String = "",
    val name: TextResource? = null,
    val userPhoto: String? = null
  )

  public interface Launcher {
    public fun onNavUp(): Boolean
    public fun onUpload(): Boolean
  }
}
