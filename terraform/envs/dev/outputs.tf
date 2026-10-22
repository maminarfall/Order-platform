output "cluster_name" {
  value = module.eks.cluster_name
}

output "configure_kubectl" {
  description = "Run this to connect kubectl to the cluster"
  value       = "aws eks update-kubeconfig --region ${var.region} --name ${module.eks.cluster_name}"
}

output "ecr_repository_urls" {
  value = module.ecr.repository_urls
}

output "orders_topic_arn" {
  value = module.messaging.topic_arn
}

output "orders_queue_url" {
  value = module.messaging.queue_url
}

output "order_service_role_arn" {
  description = "Goes on the order-service ServiceAccount annotation (Step 3)"
  value       = module.order_service_irsa.iam_role_arn
}

output "notification_service_role_arn" {
  description = "Goes on the notification-service ServiceAccount annotation (Step 3)"
  value       = module.notification_service_irsa.iam_role_arn
}
