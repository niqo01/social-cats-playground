package com.nicolasmilliard.socialcatsaws.imageupload

import coil.annotation.ExperimentalCoilApi
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.size.PixelSize
import com.nicolasmilliard.sharp.Edits
import com.nicolasmilliard.sharp.Resize
import com.nicolasmilliard.sharp.Sharp
import com.nicolasmilliard.sharp.toUrl

@OptIn(ExperimentalCoilApi::class)
public class SharpInterceptor(
  private val backendImageUrl: String
) : Interceptor {
  override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
    val size = chain.size
    if (size is PixelSize) {
      val sharp = chain.request.data as Sharp
      val resize = Resize(width = size.width, height = size.height)
      val newSharp = sharp.copy(
        edits = if (sharp.edits == null) sharp.edits?.copy(resize = resize) else Edits(
          resize = resize
        )
      )

      val request = chain.request.newBuilder()
        .data(newSharp.toUrl(backendImageUrl))
        .build()
      return chain.proceed(request)
    }
    return chain.proceed(chain.request)
  }
}
