package com.nicolasmilliard.sharp

import okio.ByteString.Companion.encodeUtf8

public fun sharp(vararg lambda: SharpBuilder.() -> Unit): Sharp {
  val sharpBuilder = SharpBuilder()
  lambda.forEach {
    sharpBuilder.apply(it)
  }
  return sharpBuilder.build()
}

public data class Sharp(
  val key: String,
  val edits: Edits?
)

public fun Sharp.toUrl(backendUrl: String): String {
  var value = "{ 'key': '$key'"
  val editsValue = edits?.serialize()
  if (editsValue != null) {
    value += ", $editsValue"
  }
  value += " }"
  val json = value.replace("'", "\"")
  return "$backendUrl/${json.encodeUtf8().base64Url()}"
}

public class SharpBuilder(
  public var key: String = "",
  private var edits: EditBuilder? = null
) {

  public fun edits(lambda: EditBuilder.() -> Unit) {
    if (edits == null) {
      edits = EditBuilder()
    }
    this.edits?.apply(lambda)
  }

  public fun build(): Sharp = Sharp(key, edits?.build())
}

public data class Edits(
  val resize: Resize? = null,
  val contentModeration: Boolean? = null,
  val roundCrop: Boolean? = null,
)

public fun Edits.serialize(): String {

  var value = "'edits': { "
  if (contentModeration != null) {
    value += "'contentModeration': $contentModeration"
  }
  if (roundCrop != null) {
    value += "'roundCrop': $roundCrop"
  }
  if (resize != null) {
    value += resize.serialize()
  }

  value += "}"
  return value
}

public data class Resize(val width: Int, val height: Int)

public fun Resize.serialize(): String =
  "'resize': { 'width': $width, 'height': $height }"

public class EditBuilder(
  private var resize: ResizeEditBuilder? = null,
  public var contentModeration: Boolean = true,
  public var roundCrop: Boolean = false
) {
  public fun resize(lambda: ResizeEditBuilder.() -> Unit) {
    if (resize == null) {
      resize = ResizeEditBuilder()
    }
    this.resize?.apply(lambda)
  }

  public fun build(): Edits = Edits(resize?.build(), contentModeration, roundCrop)
}

public class ResizeEditBuilder(
  public var width: Int = 0,
  public var height: Int = 0
) {

  public fun build(): Resize = Resize(width, height)
}
