package com.nicolasmilliard.socialcatsaws.imageupload

import android.app.Activity
import android.content.Intent
import com.github.dhaval2404.imagepicker.ImagePicker
import timber.log.Timber

public class ImageUploadNav {

  public fun startPickerFlow(activity: Activity) {
    ImagePicker.with(activity)
      .crop() // Crop profile.image(Optional), Check Customization for more option
      .compress(5120) // Final profile.image size will be less than 1 MB(Optional)
      .cameraOnly()
      .start()
  }

  public fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): String? {
    if (requestCode == ImagePicker.REQUEST_CODE) {
      when (resultCode) {
        Activity.RESULT_OK -> {
          return ImagePicker.getFilePath(data)!!
        }
        ImagePicker.RESULT_ERROR -> {
          Timber.e("Image Picker Error: ${ImagePicker.getError(data)}")
        }
        else -> {
          Timber.i("Image picker activity canceled")
        }
      }
    }
    return null
  }
}
