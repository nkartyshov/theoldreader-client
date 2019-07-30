package ru.oldowl.core.ui.view

import android.content.Context
import com.google.android.material.button.MaterialButton
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import android.util.AttributeSet
import ru.oldowl.R

class ProgressButton @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet? = null)
    : MaterialButton(context, attributeSet) {

    private val progressDrawable: AnimatedVectorDrawableCompat? = AnimatedVectorDrawableCompat.create(context, R.drawable.progress_indicator)

    private var progress: Boolean = false
    private var buttonText: CharSequence? = null

    init {
        iconGravity = ICON_GRAVITY_TEXT_START
    }

    fun setProgress(value: Boolean) {
        if (progress == value) {
            return
        }

        progress = value

        hideTitle(progress)
        showProgress(progress)
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            icon = progressDrawable
            progressDrawable?.start()
        } else {
            progressDrawable?.stop()
            icon = null
        }
    }

    private fun hideTitle(hide: Boolean) {
        if (hide) {
            buttonText = text
            text = ""
        } else {
            text = buttonText
        }
    }
}