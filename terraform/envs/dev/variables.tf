variable "region" {
  type    = string
  default = "us-east-1"
}

variable "project" {
  type    = string
  default = "order-platform"
}

variable "environment" {
  type    = string
  default = "dev"
}

variable "k8s_namespace" {
  description = "Kubernetes namespace where the services will run"
  type        = string
  default     = "orders"
}
