# <img src="./react-chanalyzer/src/logos/youtube-chanalyzer-logo.png" width="23" alt="YouTube Chanalyzer logo"> YouTube Chanalyzer
_**YouTube channel analytics tool**_

<img src="./spring-boot-chanalyzer/badges/jacoco.svg" style="display: flex;" alt="jacoco-test-coverage-badge">

<a href="https://youtube-chanalyzer.com" target="_blank" rel="noreferrer">youtube-chanalyzer.com</a>

## Contents
* [Tech Stack](#tech-stack)
* [Architecture](#architecture)
* [How It Works](#how-it-works)
* [Challenges & Resolutions](#challenges--resolutions)
* [How To Run](#how-to-run)
  * [Docker Compose (recommended)](#docker-compose-recommended)
  * [Minikube](#minikube)
  * [Deploy to Kubernetes (Amazon EKS)](#deploy-to-kubernetes-amazon-eks)
* [Future Updates & Improvements](#future-updates--improvements)

## Tech Stack
- Java 23 (OpenJDK)
- Spring Boot 3
- Playwright for Java
- React 18
- Docker
- Kubernetes

## Architecture

<img src="./YouTube%20Chanalyzer%20Diagram.svg" width="1000" style="border-radius: 10px;" alt="YouTube Chanalyzer architecture diagram">

## How It Works

The user is presented with a search bar where they can submit a YouTube channel name (e.g. `@NASA`).
On submit, the React application validates the user's input and sends a GET request to the 
Spring Boot middleware application which acts as a bridge between the client and the Playwright web scraper. 

The Spring Boot middleware system uses a reactive programming model to make requests to the Playwright for Java web scraper
(more specifically it does this using Java's WebClient, Flux and Mono APIs and parallelizes all the requests it has to make).

The results of these requests are returned to the user's browser (the React application) via an open SSE (Server-Sent Events) connection.

This provides real-time updates to the client from the server and allows for a non-blocking flow of data from the server to the client
(i.e. the client doesn't have to wait until all requests are executed on the server, it receives and displays them as they are pushed from the server).

## How To Run

### Docker Compose (recommended)

<details open>
<summary>Docker Compose run instructions</summary><br />

Execute the following command at the root of this project
```shell
make
```

Open your browser at `http://localhost:3000` to see the application running.


To shut down
```shell
make down-local
```
</details>

### Minikube

<details>
<summary>Minikube run instructions</summary><br />

Start Minikube
```shell
minikube start
```

Create the `yt-chanalyzer-ns` namespace
```shell
kubectl create namespace yt-chanalyzer-ns
```

Start the pods
```shell
kubectl apply -f kubernetes
```

Execute this command to see the pods starting up
```shell
kubectl get pods --watch
```

Expose the URL
```shell
minikube service react-chanalyzer --url -n yt-chanalyzer-ns
```
You will see output similar to the following
```shell
http://127.0.0.1:59153
❗  Because you are using a Docker driver on darwin, the terminal needs to be open to run it.
```
Copy the output address into your browser and you will see the app running
</details>

### Deploy to Kubernetes (Amazon EKS)

<details>
<summary>EKS deployment instructions</summary><br />

_The following section assumes you have some familiarity with AWS and Kubernetes/EKS_ 

Create a Kubernetes cluster (this process can take 15 - 20 mins)
```shell
eksctl create cluster --region=eu-west-2 --name=yt-chanalyzer --nodes=1 --node-type=t2.small
```

Switch to the correct context for your new cluster
```shell
aws eks update-kubeconfig --name yt-chanalyzer --region eu-west-2
```

Associate the OIDC provider
```shell
eksctl utils associate-iam-oidc-provider --cluster yt-chanalyzer-cluster --approve --region eu-west-2
```

Create the IAM policy
```shell
aws iam create-policy \
    --policy-name AWSLoadBalancerControllerIAMPolicy \
    --policy-document file://iam_policy.json
```

Create the IAM Service Account (replace `<ACCOUNT_ID>` with your account ID)
```shell
eksctl create iamserviceaccount \
  --cluster=yt-chanalyzer \
  --namespace=kube-system \
  --region=eu-west-2 \
  --name=aws-load-balancer-controller \
  --role-name AmazonEKSLoadBalancerControllerRole \
  --attach-policy-arn=arn:aws:iam::<ACCOUNT_ID>:policy/AWSLoadBalancerControllerIAMPolicy \
  --override-existing-serviceaccounts \
  --approve
```

Add the helm chart for creating the controller
```shell
helm repo add eks https://aws.github.io/eks-charts
```

Check for udpates to helm chart
```shell
helm repo update eks
```

Install the AWS load balancer controller with the helm chart (replace `<VPC_ID>` with the VPC ID of your Kubernetes cluster)
```shell
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=yt-chanalyzer \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller \
  --set region=eu-west-2 \
  --set vpcId=<VPC_ID>
```

Deploy the application
```shell
kubectl apply -f kuberenetes
```

The application will now be live, execute the following command to get its web address
```shell
kubes get ingress
```

You will see output similar to the following
```shell
NAME                    CLASS   HOSTS   ADDRESS                                                                  PORTS   AGE
ingress-yt-chanalyzer   alb     *       k8s-ytchanal-ingressy-88dc9dd409-569757692.eu-west-2.elb.amazonaws.com   80      1m
```

Copy the address from the `ADDRESS` column into your browser (if your browser enforces https make sure to manually change it to http as we have not set up an SSL certificate for the application yet)

</details>

## Future Updates & Improvements
- CI/CD pipeline with Jenkins and GitHub Actions
- End-to-end testing
- Semantic versioning
- Add linters