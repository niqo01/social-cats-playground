package com.nicolasmilliard.socialcatsaws.profile

import com.nicolasmilliard.socialcatsaws.profile.model.Avatar
import com.nicolasmilliard.socialcatsaws.profile.model.Image
import com.nicolasmilliard.socialcatsaws.profile.repository.UsersRepository
import mu.KotlinLogging
import javax.inject.Inject

private val logger = KotlinLogging.logger {}
public class UpdateUserUseCase @Inject constructor(private val usersRepository: UsersRepository) {

  public fun updateAvatar(image: Image) {
    val user = usersRepository.getUserById(image.userId)
    if (user.avatar?.imageId == image.id) {
      throw IllegalStateException("Trying ot update and url to the same one")
    }
    usersRepository.updateUser(user.copy(avatar = Avatar(image.id)))
    logger.info { "event=avatar_updated" }
  }
}
