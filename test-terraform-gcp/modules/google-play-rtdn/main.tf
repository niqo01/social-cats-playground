terraform {
  required_providers {
    aws = {
      source  = "hashicorp/google"
      version = ">= 3.85.0"
    }
  }
}

resource "google_service_account" "rtdn_oidc" {
  account_id   = "rtdn-oidc"
  display_name = "OIDC Generator Service Account"
  description  = "Used to generate OIDC token for Play Store Real time developer notification"
}

resource "google_service_account_iam_member" "admin_account_iam" {
  service_account_id = google_service_account.rtdn_oidc.name
  role               = "roles/iam.serviceAccountTokenCreator"
  member             = "serviceAccount:${google_service_account.rtdn_oidc.email}"
}

resource "google_project_service" "pub_sub_api" {
  project = var.gcp_project
  service = "pubsub.googleapis.com"
  disable_on_destroy = false
}


resource "google_pubsub_topic" "rtdn" {
  name = "rtdn-topic"

  labels = {
    stage = var.stage_name
  }
}

resource "google_pubsub_subscription" "rtdn" {
  name  = "rtdn-subscription"
  topic = google_pubsub_topic.rtdn.name

  labels = {
    stage = var.stage_name
  }

  push_config {
    push_endpoint = var.rtdn_push_url

    attributes = {
      x-goog-version = "v1"
    }

    oidc_token {
      service_account_email = google_service_account.rtdn_oidc.email
    }
  }

  message_retention_duration = "604800s"
  retain_acked_messages      = false

  ack_deadline_seconds = 40

  expiration_policy {
    ttl = ""
  }

  retry_policy {
    minimum_backoff = "2s"
    maximum_backoff = "600s"
  }

  enable_message_ordering = false
}

resource "google_pubsub_topic_iam_member" "google_play" {
  project = var.gcp_project
  topic = google_pubsub_topic.rtdn.name
  role = "roles/pubsub.publisher"
  member = "serviceAccount:google-play-developer-notifications@system.gserviceaccount.com"
}
