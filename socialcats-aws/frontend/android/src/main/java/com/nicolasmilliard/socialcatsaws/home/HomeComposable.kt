package com.nicolasmilliard.socialcatsaws.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalAirport
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nicolasmilliard.socialcatsaws.R
import com.nicolasmilliard.socialcatsaws.home.HomePresenter.Event
import com.nicolasmilliard.socialcatsaws.home.HomePresenter.Model
import com.nicolasmilliard.socialcatsaws.ui.SocialCatsAwsTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@ExperimentalCoroutinesApi
@Composable
fun Home(
  models: StateFlow<Model>,
  events: (Event) -> Unit
) {
  val model: Model by models.collectAsState()
  Scaffold(topBar = { AppBar() }) { innerPadding ->
    rememberScrollState(0f)
    LazyColumn(
      contentPadding = innerPadding,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // use `item` for separate elements like headers
      // and `items` for lists of identical elements
      item {
        Text(text = if (model.isLoading) "Loading" else "Loaded")
        Button(onClick = { events(Event.OnProfileClicked("1")) }) {
          Text(text = "View Profile")
        }
        if (model.isSignedIn) {
          Button(onClick = { events(Event.OnSignOutClicked) }) {
            Text(text = "Sign Out")
          }
        } else {
          Button(onClick = { events(Event.OnSignInClicked) }) {
            Text(text = "Sign In")
          }
        }
      }
    }
  }
}

@Composable
private fun AppBar() {
  TopAppBar(
    navigationIcon = {
      Icon(imageVector = Icons.Rounded.LocalAirport, contentDescription = null)
    },
    title = {
      Text(text = stringResource(R.string.app_name))
    },
    backgroundColor = MaterialTheme.colors.primarySurface
  )
}

@Preview(name = "Home")
@Composable
fun DefaultPreview() {
  SocialCatsAwsTheme {
    Home(viewModel()) {}
  }
}
