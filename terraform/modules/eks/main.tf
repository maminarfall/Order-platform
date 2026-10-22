module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.24"

  cluster_name    = var.cluster_name
  cluster_version = var.cluster_version

  vpc_id     = var.vpc_id
  subnet_ids = var.private_subnet_ids

  # Public endpoint so you can run kubectl from your laptop
  cluster_endpoint_public_access = true

  # Gives the person who runs terraform admin access to the cluster
  enable_cluster_creator_admin_permissions = true

  # IRSA: lets pods assume IAM roles (no AWS keys in the app)
  enable_irsa = true

  cluster_addons = {
    coredns    = {}
    kube-proxy = {}
    vpc-cni    = {}
  }

  eks_managed_node_groups = {
    default = {
      instance_types = var.node_instance_types
      min_size       = 1
      max_size       = 3
      desired_size   = var.node_desired_size
    }
  }

  tags = var.tags
}
