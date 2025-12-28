# AWS ECS Deployment Guide

This guide covers deploying the Employee Mongo CRUD API to AWS ECS with DocumentDB (MongoDB-compatible).

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                 AWS Cloud                                    │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                                  VPC                                     ││
│  │  ┌─────────────────────────────┐   ┌───────────────────────────────────┐││
│  │  │      Public Subnets         │   │        Private Subnets            │││
│  │  │  ┌─────────────────────┐   │   │  ┌────────┐    ┌──────────────┐  │││
│  │  │  │   Load Balancer     │   │   │  │  ECS   │───▶│  DocumentDB  │  │││
│  │  │  │   (ALB)             │───┼───┼▶│ Fargate │    │  Cluster     │  │││
│  │  │  └─────────────────────┘   │   │  └────────┘    └──────────────┘  │││
│  │  │                            │   │       │                           │││
│  │  │  ┌─────────────────────┐   │   │       ▼                           │││
│  │  │  │   NAT Gateway       │◀──┼───┼───────┘                           │││
│  │  │  └─────────────────────┘   │   │                                   │││
│  │  └────────────────────────────┘   └───────────────────────────────────┘││
│  └─────────────────────────────────────────────────────────────────────────┘│
│                                                                              │
│  ┌─────────────┐  ┌─────────────────┐  ┌─────────────────┐                 │
│  │     ECR     │  │ Secrets Manager │  │  CloudWatch     │                 │
│  │  (Images)   │  │  (Credentials)  │  │  (Logs/Metrics) │                 │
│  └─────────────┘  └─────────────────┘  └─────────────────┘                 │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Prerequisites

1. **AWS CLI** configured with appropriate credentials
2. **Terraform** >= 1.0 installed
3. **Docker** installed locally
4. **GitHub repository** with Actions enabled

## Step 1: Set Up GitHub Secrets

Add these secrets to your GitHub repository (`Settings > Secrets and variables > Actions`):

| Secret Name | Description |
|------------|-------------|
| `AWS_ROLE_ARN` | ARN of the IAM role for GitHub Actions (OIDC) |
| `AWS_ACCESS_KEY_ID` | (Alternative) AWS access key |
| `AWS_SECRET_ACCESS_KEY` | (Alternative) AWS secret key |

### Setting up OIDC (Recommended)

```bash
# Create OIDC identity provider in AWS
aws iam create-open-id-connect-provider \
  --url https://token.actions.githubusercontent.com \
  --thumbprint-list "6938fd4d98bab03faadb97b34396831e3780aea1" \
  --client-id-list "sts.amazonaws.com"
```

## Step 2: Deploy Infrastructure with Terraform

```bash
cd .aws

# Initialize Terraform
terraform init

# Create terraform.tfvars with your values
cat > terraform.tfvars << EOF
aws_region            = "us-east-1"
environment           = "prod"
docdb_master_username = "admin"
docdb_master_password = "YourSecurePassword123!"  # Change this!
jwt_secret            = "your-super-secret-jwt-key-min-32-chars"
EOF

# Review the plan
terraform plan

# Apply the infrastructure
terraform apply
```

## Step 3: Download DocumentDB Certificate

DocumentDB requires TLS. Download the CA bundle:

```bash
# The Dockerfile should include this, but for reference:
curl -o rds-combined-ca-bundle.pem \
  https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem
```

Update the Dockerfile to include the certificate:

```dockerfile
# Add after COPY --from=builder line
RUN wget -O /app/rds-combined-ca-bundle.pem \
    https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem
```

## Step 4: First Deployment

After Terraform creates the infrastructure, the GitHub Actions workflow will:

1. Run tests with MongoDB service container
2. Build Docker image
3. Push to ECR
4. Deploy to ECS

Trigger by pushing to `main`:

```bash
git add .
git commit -m "Add ECS deployment configuration"
git push origin main
```

## Step 5: Verify Deployment

```bash
# Get the ALB DNS name
terraform output alb_dns_name

# Test the health endpoint
curl http://<ALB_DNS_NAME>/actuator/health

# Test the API
curl -X POST http://<ALB_DNS_NAME>/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "YOUR_AUTH_PASSWORD"}'
```

## MongoDB Options

### Option A: Amazon DocumentDB (Recommended for AWS)

- Fully managed, MongoDB-compatible
- Automatic backups, scaling
- High availability with replica sets
- Included in the Terraform config

### Option B: MongoDB Atlas

If you prefer MongoDB Atlas:

1. Create a MongoDB Atlas cluster
2. Peer the Atlas VPC with your AWS VPC
3. Update secrets in Secrets Manager:

```bash
aws secretsmanager update-secret \
  --secret-id employee-mongo/db \
  --secret-string '{
    "host": "your-cluster.mongodb.net",
    "port": "27017",
    "database": "mydatabase",
    "username": "your-user",
    "password": "your-password"
  }'
```

### Option C: Self-hosted MongoDB on ECS

Add a MongoDB container to your ECS task definition. Not recommended for production due to persistence challenges.

## Environment Configuration

| Environment Variable | Description | Source |
|---------------------|-------------|--------|
| `MONGO_HOST` | MongoDB/DocumentDB endpoint | Secrets Manager |
| `MONGO_PORT` | Database port (27017) | Secrets Manager |
| `MONGO_DATABASE` | Database name | Secrets Manager |
| `MONGO_USERNAME` | Database username | Secrets Manager |
| `MONGO_PASSWORD` | Database password | Secrets Manager |
| `JWT_SECRET` | JWT signing key | Secrets Manager |
| `AUTH_USERNAME` | API auth username | Secrets Manager |
| `AUTH_PASSWORD` | API auth password | Secrets Manager |

## Monitoring & Logs

### CloudWatch Logs

```bash
# View logs
aws logs tail /ecs/employee-mongo-api --follow
```

### Container Insights

Enabled by default. View in CloudWatch > Container Insights.

### Health Checks

- **ALB Health Check**: `GET /actuator/health`
- **ECS Container Health Check**: `wget` to `/actuator/health`

## Scaling

### Manual Scaling

```bash
aws ecs update-service \
  --cluster employee-mongo-cluster \
  --service employee-mongo-service \
  --desired-count 3
```

### Auto Scaling (Add to Terraform)

```hcl
resource "aws_appautoscaling_target" "ecs" {
  max_capacity       = 10
  min_capacity       = 2
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.app.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "cpu" {
  name               = "cpu-autoscaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value = 70.0
  }
}
```

## Troubleshooting

### ECS Task Won't Start

```bash
# Check task stopped reason
aws ecs describe-tasks \
  --cluster employee-mongo-cluster \
  --tasks <TASK_ARN>

# Check logs
aws logs tail /ecs/employee-mongo-api --since 1h
```

### Database Connection Issues

1. Verify security groups allow traffic on port 27017
2. Check DocumentDB cluster is running
3. Verify TLS certificate is included in container
4. Check secrets are correctly configured

### Health Check Failures

1. Ensure actuator endpoints are exposed
2. Check container logs for startup errors
3. Verify security config allows `/actuator/health` unauthenticated

## Cost Optimization

| Resource | Dev/Test | Production |
|----------|----------|------------|
| ECS Fargate | 1 task, 0.5 vCPU, 1GB | 2+ tasks, 1 vCPU, 2GB |
| DocumentDB | db.t3.medium | db.r6g.large (HA) |
| NAT Gateway | Single | Multi-AZ |

Estimated monthly costs:
- **Dev**: ~$80-120/month
- **Production**: ~$300-500/month

## Cleanup

```bash
# Destroy all resources
cd .aws
terraform destroy
```

