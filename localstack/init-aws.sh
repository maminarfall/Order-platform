#!/bin/bash
# Runs inside LocalStack on startup: creates the same SNS -> SQS wiring Terraform will build in AWS.
set -e
awslocal sqs create-queue --queue-name order-events-dlq
DLQ_ARN=$(awslocal sqs get-queue-attributes --queue-url http://localhost:4566/000000000000/order-events-dlq \
  --attribute-names QueueArn --query Attributes.QueueArn --output text)

awslocal sqs create-queue --queue-name order-events \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$DLQ_ARN\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}\"}"

awslocal sns create-topic --name order-created
awslocal sns subscribe \
  --topic-arn arn:aws:sns:us-east-1:000000000000:order-created \
  --protocol sqs \
  --notification-endpoint arn:aws:sqs:us-east-1:000000000000:order-events
echo "SNS/SQS ready"
