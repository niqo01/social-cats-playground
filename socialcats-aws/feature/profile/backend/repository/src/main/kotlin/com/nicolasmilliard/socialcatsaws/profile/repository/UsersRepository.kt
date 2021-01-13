package com.nicolasmilliard.socialcatsaws.profile.repository

import com.nicolasmilliard.socialcatsaws.profile.model.Image
import com.nicolasmilliard.socialcatsaws.profile.model.User
import com.nicolasmilliard.socialcatsaws.profile.model.UserWithImages

public interface UsersRepository {
  public fun getUserAndMessages(userId: String, newestImagesCount: Int): UserWithImages

  public fun insertUser(user: User)
  public fun updateUser(user: User)

  public fun getUserById(id: String): User

  public fun insertImage(image: Image)
  public fun countImages(userId: String): Int

  public fun deleteAllItems()
}
