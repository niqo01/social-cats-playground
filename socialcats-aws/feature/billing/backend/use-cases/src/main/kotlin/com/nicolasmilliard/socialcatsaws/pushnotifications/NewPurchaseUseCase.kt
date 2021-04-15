package com.nicolasmilliard.socialcatsaws.billing

import com.nicolasmilliard.cloudmetric.CloudMetrics
import com.nicolasmilliard.cloudmetric.Unit
import com.nicolasmilliard.socialcatsaws.profile.model.Device
import com.nicolasmilliard.socialcatsaws.profile.repository.UsersRepository
import mu.KotlinLogging
import javax.inject.Inject

private val logger = KotlinLogging.logger {}
public class NewPurchaseUseCase @Inject constructor (private val usersRepository: UsersRepository, private val cloudMetrics: CloudMetrics) {
  public fun processPurchase(device: Device) {
    usersRepository.insertDevice(device)
    logger.info("event=repository_new_device")
    cloudMetrics.putMetric("NewDeviceCount", 1.0, Unit.COUNT)
  }
}
