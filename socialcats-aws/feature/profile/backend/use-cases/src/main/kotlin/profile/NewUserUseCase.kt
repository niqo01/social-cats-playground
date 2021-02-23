package com.nicolasmilliard.socialcatsaws.profile

import com.nicolasmilliard.socialcatsaws.profile.model.User
import com.nicolasmilliard.socialcatsaws.profile.repository.UsersRepository
import kotlinx.datetime.Clock
import mu.KotlinLogging
import javax.inject.Inject

private val logger = KotlinLogging.logger {}
public class NewUserUseCase @Inject constructor (private val usersRepository: UsersRepository) {
  public fun onNewAuthUser(userId: String, email: String, emailVerified: Boolean) {
    usersRepository.updateUser(User(id = userId, Clock.System.now(), email = email, emailVerified = emailVerified))
    logger.info("event=repository_new_user")
  }
}
