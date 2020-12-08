package com.nicolasmilliard.socialcats.search.ui

import android.view.View
import android.view.View.OnClickListener
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.LoadRequest
import coil.transform.CircleCropTransformation
import com.nicolasmilliard.socialcats.model.User
import com.nicolasmilliard.socialcats.search.ui.databinding.UserBinding

internal class UserResultViewHolder(
    private val binding: UserBinding,
    private val imageLoader: ImageLoader,
    private val callback: UserResultAdapter.Callback
) : RecyclerView.ViewHolder(binding.root), OnClickListener {

    init {
        binding.root.setOnClickListener(this)
    }

    private var user: User? = null

    override fun onClick(view: View) {
        callback.onUserClicked(user!!)
    }

    fun update(userResult: UserResult) {
        val user = userResult.user
        this.user = user

        val request = LoadRequest.Builder(binding.userPhoto.context)
            .data(user.photoUrl)
            .target(binding.userPhoto)
            .transformations(CircleCropTransformation())
            .build()
        imageLoader.execute(request)

        binding.userName.text = user.name
        binding.userAdditional.text = user.id
    }
}
