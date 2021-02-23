package com.nicolasmilliard.activityresult

import android.content.Intent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

public class ActivityResultFlow {
  private val _events = MutableSharedFlow<Event>()
  public val events: Flow<Event> = _events.asSharedFlow() // publicly exposed as read-only shared flow

  public suspend fun produceEvent(event: Event) {
    _events.emit(event) // suspends until all subscribers receive it
  }
}

public data class Event(
  val requestCode: Int,
  val resultCode: Int,
  val data: Intent?
)
