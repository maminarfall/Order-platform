# Event-Driven Order Platform on AWS

Two Java (Spring Boot) microservices that talk through SNS → SQS events.
Built step by step: local first, then AWS (EKS, API Gateway, CloudFront, WAF) with Terraform and GitLab CI.

## Architecture (final goal)

```
Client → CloudFront + WAF → API Gateway → NGINX Ingress (EKS) → Order Service
                                                                    │ publishes "OrderCreated"
                                                                    ▼
                                                              SNS topic
                                                                    │
                                                                    ▼
                                                     SQS queue (+ DLQ after 3 failures)
                                                                    │
                                                                    ▼
                                                       Notification Service (EKS)
```

## Progress

- [x] **Step 1** – Java services + Docker + local SNS/SQS (LocalStack)
- [ ] Step 2 – Terraform: VPC, EKS, ECR, SNS, SQS
- [ ] Step 3 – Helm charts + NGINX Ingress on EKS
- [ ] Step 4 – API Gateway, CloudFront, WAF
- [ ] Step 5 – GitLab CI pipeline
- [ ] Step 6 – Prometheus + Grafana + alert

## Step 1: Run it locally

You need Docker Desktop. That's it (Java and Maven run inside Docker).

```bash
docker compose up --build
```

Wait until you see `SNS/SQS ready` and both services start.

**Create an order:**
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerEmail":"nay@example.com","item":"laptop","quantity":1,"price":999.99}'
```

**Check it worked:** in the `notification-service` logs you should see:
```
NOTIFICATION -> to=nay@example.com : Your order ... for 1 x laptop was received
```

**Other endpoints:**
| URL | What it does |
|---|---|
| `GET  localhost:8081/api/orders` | List orders |
| `GET  localhost:8081/api/orders/{id}` | One order |
| `GET  localhost:8081/actuator/health` | Health check (used by Kubernetes probes later) |
| `GET  localhost:8082/actuator/prometheus` | Metrics (`notifications_processed_total`, etc.) |

**Run unit tests** (needs Java 21 + Maven): `cd order-service && mvn test`

## Project layout

```
order-service/          REST API, publishes events to SNS
notification-service/   Reads events from SQS, logs a notification
localstack/init-aws.sh  Creates topic, queue, DLQ, subscription locally
docker-compose.yml      Runs everything together
```

## Design choices (good interview talking points)

- **SNS → SQS (fan-out)** instead of calling the other service directly: services are decoupled, and new consumers can subscribe later without changing Order Service.
- **Dead-letter queue**: a message that fails 3 times goes to the DLQ instead of blocking the queue.
- **Message deleted only after success**: failed messages are retried automatically by SQS.
- **Long polling** (`waitTimeSeconds=10`): fewer empty calls to SQS, lower cost.
- **Same code locally and in AWS**: only `AWS_ENDPOINT` changes. On EKS, credentials come from IRSA (IAM role for the service account), no keys in code.
- **Multi-stage Docker build + non-root user**: small, safer images.
- **Actuator health probes + Prometheus metrics** ready for Kubernetes and Grafana.
