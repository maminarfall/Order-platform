# Step 3: Deploy to EKS

Run everything from the root of the repo, in Git Bash.

## 1. Connect kubectl
```bash
aws eks update-kubeconfig --region us-east-1 --name order-platform-dev
kubectl get nodes          # 2 nodes "Ready"
```

## 2. Get your Terraform values
```bash
cd terraform/envs/dev
terraform output
cd ../../..
```
Copy the values into the CHANGE-ME spots in `deploy/dev/order-service.yaml`
and `deploy/dev/notification-service.yaml`.
cd terraform/envs/dev
terraform output

## 3. Push the images to ECR
```bash
ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
REGISTRY=$ACCOUNT.dkr.ecr.us-east-1.amazonaws.com

aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $REGISTRY

docker build -t $REGISTRY/order-service:v1 ./order-service
docker push $REGISTRY/order-service:v1

docker build -t $REGISTRY/notification-service:v1 ./notification-service
docker push $REGISTRY/notification-service:v1
```

## 4. Install the reverse proxy (Traefik ingress controller)
```bash
helm repo add traefik https://traefik.github.io/charts
helm repo update
helm install traefik traefik/traefik -n traefik --create-namespace
```

## 5. Deploy the 2 services
```bash
helm upgrade --install order-service ./charts/app -n orders --create-namespace -f deploy/dev/order-service.yaml
helm upgrade --install notification-service ./charts/app -n orders -f deploy/dev/notification-service.yaml

kubectl get pods -n orders -w      # wait for all pods READY 1/1, then Ctrl+C
```

## 6. Test it
```bash
LB=$(kubectl get svc traefik -n traefik -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
echo $LB      # the load balancer can take 2-3 minutes to work

curl -X POST http://$LB/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerEmail":"nay@example.com","item":"laptop","quantity":1,"price":999.99}'

kubectl logs -n orders deploy/notification-service | grep NOTIFICATION
```

## Troubleshooting
| Problem | Command |
|---|---|
| Pod not starting | `kubectl describe pod -n orders <pod-name>` |
| App error | `kubectl logs -n orders <pod-name>` |
| AccessDenied for SNS/SQS | check `roleArn` in the values file, then `helm upgrade` again |
