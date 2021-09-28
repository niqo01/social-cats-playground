output "rtdn_topic_id" {
  value       = module.google_play_rtdn.rtdn_topic_id
  description = "Rtdn Pub/Sub Topic Id"
}
output "play_billing_client_email" {
  value       = google_service_account.play_billing.email
  description = "Service Account email used to connect to the Play Billing API"
}
