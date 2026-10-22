variable "name" {
  description = "Name prefix for SNS/SQS resources"
  type        = string
}

variable "max_receive_count" {
  description = "Failed receives before a message moves to the DLQ"
  type        = number
  default     = 3
}

variable "tags" {
  type    = map(string)
  default = {}
}
