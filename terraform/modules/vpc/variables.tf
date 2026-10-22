variable "name" {
  description = "Name prefix for the VPC"
  type        = string
}

variable "cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

variable "tags" {
  type    = map(string)
  default = {}
}
