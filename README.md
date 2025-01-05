# <img src="./react-chanalyzer/src/logos/youtube-chanalyzer-logo.png" width="23" alt="YouTube Chanalyzer logo"> YouTube Chanalyzer
_**YouTube channel analytics tool**_

<a href="https://youtube-chanalyzer.com" target="_blank" rel="noreferrer">youtube-chanalyzer.com</a>

## Tech Stack
- Java 23 (OpenJDK)
- Spring Boot 3
- Python 3
- React 18
- Docker
- Kubernetes

## Architecture

<img src="./YouTube%20Chanalyzer%20Diagram.svg" width="1000" style="border-radius: 10px;" alt="YouTube Chanalyzer architecture diagram">

## How It Works

The Spring Boot middleware system uses a reactive programming model to make requests to the Python web scraper (more specifically it does this using Java's WebClient, Flux and Mono APIs and parallelizes all the requests it has to make).

The result of these requests are returned to the user's browser (the React application) via an open SSE (Server-Sent Events) connection.

This provides real-time updates to the client from the server and allows for a non-blocking flow of data from the server to the client (i.e. the client doesn't have to wait until all requests are executed on the server, it receives and displays them as they are pushed from the server).

## How To Run

### Docker Compose

<details open>
<summary>Docker Compose run instructions</summary><br />

Execute the following command at the root of this project
```shell
make
```

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

## Future Updates & Improvements
- CI/CD pipeline with Jenkins and GitHub Actions
- End-to-end testing
- Re-write the web scraper to be more efficient (I am interested in using Kotlin's <a href="https://github.com/skrapeit/skrape.it" target="_blank" rel="noreferrer">skrape{it}</a> library for this)
- Semantic versioning
- Add linters