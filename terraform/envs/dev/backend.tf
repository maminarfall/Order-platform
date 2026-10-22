# Remote state in S3. Create the bucket once first (see terraform/README.md),
# then replace the bucket name below with yours.
terraform {
  backend "s3" {
    bucket       = "CHANGE-ME-order-platform-tfstate"
    key          = "dev/terraform.tfstate"
    region       = "us-east-1"
    encrypt      = true
    use_lockfile = true # S3 native locking (Terraform 1.10+), no DynamoDB table needed
  }
}
