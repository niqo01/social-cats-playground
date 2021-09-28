variable "stage_name" {
  type        = string
  description = "The stage name of the environment"
}
# define GCP project name
variable "gcp_project" {
  type        = string
  description = "GCP project name"
}
# define url to push rtdn messages
variable "rtdn_push_url" {
  type        = string
  description = "Push Url destination for RTDN messages"
}

