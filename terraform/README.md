# Terraform (Step 2)

Creates: VPC, EKS cluster, 2 ECR repos, SNS topic + SQS queue + DLQ, and IAM roles for the pods (IRSA).

```
modules/
  vpc/        VPC, 2 AZs, public + private subnets, 1 NAT gateway
  eks/        EKS cluster, managed node group (t3.medium), IRSA enabled
  ecr/        ECR repos with scan-on-push and image cleanup
  messaging/  SNS topic -> SQS queue -> DLQ (after 3 failures)
envs/dev/     Calls the modules + least-privilege IAM roles for each service
```

## Before you start
- Terraform 1.10 or newer (`terraform version`)
- AWS CLI logged in (`aws sts get-caller-identity` should show your IAM user)

## 1. Create the state bucket (one time)
Bucket names must be unique worldwide, so add something of yours:
```bash
aws s3api create-bucket --bucket nay-order-platform-tfstate --region us-east-1
aws s3api put-bucket-versioning --bucket nay-order-platform-tfstate \
  --versioning-configuration Status=Enabled
```
Then put that bucket name in `envs/dev/backend.tf`.

## 2. Create everything
```bash
cd terraform/envs/dev
terraform init
terraform plan      # review what will be created (about 60 resources)
terraform apply     # takes about 15-20 minutes (EKS is slow)
```

## 3. Connect kubectl
```bash
$(terraform output -raw configure_kubectl)
kubectl get nodes   # should show 2 nodes "Ready"
```

## 4. Save money: destroy when done
EKS + NAT gateway cost roughly $5 a day. When you stop for the day:
```bash
terraform destroy
```
(The state bucket stays, so `terraform apply` brings everything back.)
