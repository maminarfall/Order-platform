output "cluster_name" {
  value = module.eks.cluster_name
}

output "cluster_endpoint" {
  value = module.eks.cluster_endpoint
}

output "oidc_provider_arn" {
  description = "Used to create IAM roles for Kubernetes service accounts (IRSA)"
  value       = module.eks.oidc_provider_arn
}
