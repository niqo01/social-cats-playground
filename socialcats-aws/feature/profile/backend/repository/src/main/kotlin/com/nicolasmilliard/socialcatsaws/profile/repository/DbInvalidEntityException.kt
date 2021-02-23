package com.nicolasmilliard.socialcatsaws.profile.repository

public class DbInvalidEntityException(msg: String, cause: Exception? = null) :
  RuntimeException(msg, cause)
