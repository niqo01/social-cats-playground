terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "3.85.0"
    }
  }
}

module "google_play_rtdn" {
  source = "./modules/google-play-rtdn"

  stage_name    = var.stage_name
  gcp_project   = var.gcp_project
  rtdn_push_url = "https://webhook.sample.com"
}


provider "google" {
  project     = var.gcp_project
  credentials = file(var.gcp_auth_file)
  region      = var.gcp_region_1
  zone        = var.gcp_zone_1
}

resource "google_service_account" "play_billing" {
  account_id   = "play-billing"
  display_name = "Play Billing Client"
  description  = "Used by 3rd party to connect to the play store Billing API"
}

resource "google_service_account_iam_member" "admin_account_iam" {
  service_account_id = google_service_account.play_billing.name
  role               = "roles/iam.serviceAccountUser"
  member             = "serviceAccount:${google_service_account.play_billing.email}"
}
