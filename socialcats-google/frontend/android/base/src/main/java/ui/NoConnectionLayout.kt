package com.nicolasmilliard.socialcats.ui

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import com.nicolasmilliard.socialcats.base.R

class NoConnectionLayout(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs) {

    lateinit var connectionSettingsButton: Button

    override fun onFinishInflate() {
        super.onFinishInflate()
        connectionSettingsButton = findViewById(R.id.check_connectivity)
        val image: ImageView = findViewById(R.id.image)
        (getDrawable(context, R.drawable.avd_no_connection) as AnimatedVectorDrawable).apply {
            image.setImageDrawable(this)
            start()
        }
    }
}
