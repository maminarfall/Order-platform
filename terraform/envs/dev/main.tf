locals {
  name = "${var.project}-${var.environment}"
  tags = {
    Project     = var.project
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

module "vpc" {
  source = "../../modules/vpc"
  name   = local.name
  tags   = local.tags
}

module "eks" {
  source             = "../../modules/eks"
  cluster_name       = local.name
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  tags               = local.tags
}

module "ecr" {
  source       = "../../modules/ecr"
  repositories = ["order-service", "notification-service"]
  tags         = local.tags
}

module "messaging" {
  source = "../../modules/messaging"
  name   = local.name
  tags   = local.tags
}

# ---------- IAM for the pods (IRSA), least privilege ----------

# Order Service may only publish to its topic
data "aws_iam_policy_document" "order_service" {
  statement {
    actions   = ["sns:Publish"]
    resources = [module.messaging.topic_arn]
  }
}

resource "aws_iam_policy" "order_service" {
  name   = "${local.name}-order-service"
  policy = data.aws_iam_policy_document.order_service.json
}

# Notification Service may only read/delete from its queue
data "aws_iam_policy_document" "notification_service" {
  statement {
    actions = [
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
    ]
    resources = [module.messaging.queue_arn]
  }
}

resource "aws_iam_policy" "notification_service" {
  name   = "${local.name}-notification-service"
  policy = data.aws_iam_policy_document.notification_service.json
}

module "order_service_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.47"

  role_name        = "${local.name}-order-service"
  role_policy_arns = { sns = aws_iam_policy.order_service.arn }

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${var.k8s_namespace}:order-service"]
    }
  }
}

module "notification_service_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.47"

  role_name        = "${local.name}-notification-service"
  role_policy_arns = { sqs = aws_iam_policy.notification_service.arn }

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${var.k8s_namespace}:notification-service"]
    }
  }
}
