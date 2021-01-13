package com.nicolasmilliard.socialcatsaws.imageupload

import com.nicolasmilliard.sharp.SharpBuilder

public fun sharpDefaults(): SharpBuilder.() -> Unit {
  return { edits { contentModeration = true } }
}
