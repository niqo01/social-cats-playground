package com.nicolasmilliard.socialcatsaws.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nicolasmilliard.sharp.sharp
import com.nicolasmilliard.socialcatsaws.billing.BillingRepository
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
                SelectionContainer {
                    Text(text = "AuthId: ${model.authId}")
                }
                if (model.showUpload) {
                    Button(onClick = { events(Event.OnUpload) }) {
                        Text(text = "Upload")
                    }
                }

                if (model.isPremium) {
                    Button(onClick = { events(Event.OnPremiumCancel) }) {
                        Text(text = "Cancel Subscription")
                    }
                }
            }
            if (!model.isPremium) {
                item { PostListSimpleSection(model.subscriptions, events) }
            }
        }
    }
}

@Composable
private fun PostListSimpleSection(
    subscriptions: List<BillingRepository.Subscription>,
    events: (Event) -> Unit
) {
    Column {
        subscriptions.forEach { subscription ->
            PostCardSimple(
                subscription = subscription,
                events
            )
            PostListDivider()
        }
    }
}

@Composable
private fun PostListDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 14.dp),
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.08f)
    )
}

@Composable
private fun PostCardSimple(
    subscription: BillingRepository.Subscription,
    events: (Event) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = { events(Event.OnSubscriptionSelected(subscription)) })
            .padding(16.dp)
    ) {
        PostImage(subscription, Modifier.padding(end = 16.dp))
        Column(modifier = Modifier.weight(1f)) {
            PostTitle(subscription)
            AuthorAndReadTime(subscription)
        }
        BookmarkButton(
            false,
            onClick = {},
            // Remove button semantics so action can be handled at row level
            modifier = Modifier.clearAndSetSemantics {}
        )
    }
}

@Composable
private fun PostTitle(post: BillingRepository.Subscription) {
    Text(post.title, style = MaterialTheme.typography.subtitle1)
}

@Composable
private fun AuthorAndReadTime(
    post: BillingRepository.Subscription,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            val textStyle = MaterialTheme.typography.body2
            Text(
                text = post.description,
                style = textStyle
            )
            Text(
                text = " - ${post.sku} min read",
                style = textStyle
            )
        }
    }
}

@Composable
private fun PostImage(post: BillingRepository.Subscription, modifier: Modifier = Modifier) {
    Image(
        imageVector = Icons.Default.Image,
        contentDescription = null, // decorative
        modifier = modifier
            .size(40.dp, 40.dp)
            .clip(MaterialTheme.shapes.small)
    )
}

@Composable
private fun BookmarkButton(
    isBookmarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    IconToggleButton(
        checked = isBookmarked,
        onCheckedChange = { onClick() },
        modifier = modifier.semantics {
            // Use a custom click label that accessibility services can communicate to the user.
            // We only want to override the label, not the actual action, so for the action we pass null.
            // this.onClick(label = clickLabel, action = null)
        }
    ) {
        Icon(
            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
            contentDescription = null // handled by click label of parent
        )
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
