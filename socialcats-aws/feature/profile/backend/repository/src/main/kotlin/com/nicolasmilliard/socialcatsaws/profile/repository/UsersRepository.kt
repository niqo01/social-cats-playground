package com.nicolasmilliard.socialcatsaws.profile.repository

import com.nicolasmilliard.socialcatsaws.profile.model.Device
import com.nicolasmilliard.socialcatsaws.profile.model.Image
import com.nicolasmilliard.socialcatsaws.profile.model.User
import com.nicolasmilliard.socialcatsaws.profile.model.UserWithImages

public interface UsersRepository {
  public fun getUserAndImages(userId: String, newestImagesCount: Int): UserWithImages

  // public fun insertUser(user: User): InsertResult
  public fun updateUser(user: User): User

  public fun getUserById(id: String): User

  public fun insertImage(image: Image): InsertResult
  public fun countImages(userId: String): Int

  public fun insertDevice(device: Device): InsertResult
  public fun updateNotificationKey(userId: String, notificationKey: String)

  public fun getTokens(userId: String): List<String>
}

public sealed class InsertResult {
  public object Added : InsertResult()
  public object AlreadyExist : InsertResult()
}
