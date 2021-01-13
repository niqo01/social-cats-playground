package com.nicolasmilliard.socialcatsaws

import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ActivityResultFlow {
  private val _events = MutableSharedFlow<Event>()
  val events = _events.asSharedFlow() // publicly exposed as read-only shared flow

  suspend fun produceEvent(event: Event) {
    _events.emit(event) // suspends until all subscribers receive it
  }
}

data class Event(
  val requestCode: Int,
  val resultCode: Int,
  val data: Intent?
)
