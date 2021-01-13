package com.nicolasmilliard.socialcatsaws.profile

import com.nicolasmilliard.socialcatsaws.profile.model.User
import com.nicolasmilliard.socialcatsaws.profile.repository.UsersRepository
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
public class NewUserUseCase(private val usersRepository: UsersRepository) {
  public fun onNewAuthUser(userId: String, email: String, emailVerified: Boolean) {
    usersRepository.insertUser(User(id = userId, email = email, emailVerified = emailVerified))
    logger.info("event=repository_new_user")
  }
}
