# Same wiring as localstack/init-aws.sh, but in real AWS:
# SNS topic -> SQS queue -> (after 3 failures) dead-letter queue

resource "aws_sns_topic" "order_created" {
  name = "${var.name}-order-created"
  tags = var.tags
}

resource "aws_sqs_queue" "dlq" {
  name                      = "${var.name}-order-events-dlq"
  message_retention_seconds = 1209600 # 14 days to investigate failures
  sqs_managed_sse_enabled   = true
  tags                      = var.tags
}

resource "aws_sqs_queue" "order_events" {
  name                       = "${var.name}-order-events"
  visibility_timeout_seconds = 30
  receive_wait_time_seconds  = 10 # long polling
  sqs_managed_sse_enabled    = true

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = var.max_receive_count
  })

  tags = var.tags
}

# Allow ONLY this SNS topic to send messages into the queue
data "aws_iam_policy_document" "queue" {
  statement {
    effect    = "Allow"
    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.order_events.arn]

    principals {
      type        = "Service"
      identifiers = ["sns.amazonaws.com"]
    }

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"
      values   = [aws_sns_topic.order_created.arn]
    }
  }
}

resource "aws_sqs_queue_policy" "order_events" {
  queue_url = aws_sqs_queue.order_events.id
  policy    = data.aws_iam_policy_document.queue.json
}

resource "aws_sns_topic_subscription" "order_events" {
  topic_arn = aws_sns_topic.order_created.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.order_events.arn
}
