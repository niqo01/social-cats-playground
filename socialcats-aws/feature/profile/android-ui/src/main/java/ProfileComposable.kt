package com.nicolasmilliard.socialcatsaws.profile

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nicolasmilliard.sharp.sharp
import com.nicolasmilliard.socialcatsaws.imageupload.sharpDefaults
import com.nicolasmilliard.socialcatsaws.profile.ProfilePresenter.Event
import com.nicolasmilliard.socialcatsaws.profile.ProfilePresenter.Model
import com.nicolasmilliard.textresource.toText
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
public fun Profile(
  models: StateFlow<Model>,
  events: (Event) -> Unit
) {
  Scaffold(
    topBar = { AppBar(models) { events(Event.OnNavUp) } }
  ) { innerPadding ->
    val model: Model by models.collectAsState()
    rememberScrollState(0)
    LazyColumn(contentPadding = innerPadding) {
      // use `item` for separate elements like headers
      // and `items` for lists of identical elements
      item {
        if (model.userPhoto != null) {
          CoilImage(
            data = sharp(sharpDefaults(), { key = model.userPhoto!! }),
            contentDescription = "Test",
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape),
          )
        }

        Text(text = "Test ${model.isLoading}, ${model.userId}")
        if (model.showUpload) {
          Button(onClick = { events(Event.OnUpload) }) {
            Text(text = "Upload")
          }
        }
      }
    }
  }
}

@Composable
private fun AppBar(models: StateFlow<Model>, onUp: () -> Unit) {
  TopAppBar(
    navigationIcon = {
      IconButton(onClick = onUp) {
        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
      }
    },
    title = {
      val model: Model by models.collectAsState()
      Text(text = "${model.name?.toText()}")
    },
    backgroundColor = MaterialTheme.colors.primarySurface
  )
}

@Preview(name = "Home")
@Composable
public fun DefaultPreview() {
  Profile(MutableStateFlow(Model())) {}
}
