output "topic_arn" {
  value = aws_sns_topic.order_created.arn
}

output "queue_url" {
  value = aws_sqs_queue.order_events.url
}

output "queue_arn" {
  value = aws_sqs_queue.order_events.arn
}

output "dlq_arn" {
  value = aws_sqs_queue.dlq.arn
}
