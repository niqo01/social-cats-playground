variable "stage_name" {
  type        = string
  description = "The stage name of the environment"
}

# define the GCP authentication file
variable "gcp_auth_file" {
  type        = string
  description = "GCP authentication file"
}
# define GCP project name
variable "gcp_project" {
  type        = string
  description = "GCP project name"
}
# define GCP region
variable "gcp_region_1" {
  type        = string
  description = "GCP region"
}
# define GCP zone
variable "gcp_zone_1" {
  type        = string
  description = "GCP zone"
}
