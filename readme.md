## Running Vert.x Microservices on Kubernetes/OpenShift [![Twitter](https://img.shields.io/twitter/follow/piotr_minkowski.svg?style=social&logo=twitter&label=Follow%20Me)](https://twitter.com/piotr_minkowski)

[![CircleCI](https://circleci.com/gh/piomin/sample-vertx-kubernetes.svg?style=svg)](https://circleci.com/gh/piomin/sample-vertx-kubernetes)

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-black.svg)](https://sonarcloud.io/dashboard?id=piomin_sample-vertx-kubernetes)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=piomin_sample-vertx-kubernetes&metric=bugs)](https://sonarcloud.io/dashboard?id=piomin_sample-vertx-kubernetes)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=piomin_sample-vertx-kubernetes&metric=coverage)](https://sonarcloud.io/dashboard?id=piomin_sample-vertx-kubernetes)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=piomin_sample-vertx-kubernetes&metric=ncloc)](https://sonarcloud.io/dashboard?id=piomin_sample-vertx-kubernetes)

Detailed description can be found here: [Running Vert.x Microservices on Kubernetes/OpenShift](https://piotrminkowski.com/2018/03/20/running-vert-x-microservices-on-kubernetes-openshift/)

In order to run the apps on Kubernetes do the following things:

Build the whole project with the following Maven command:
```shell
$ mvn clean package
```

Then build the image and deploy both apps on Kubernetes with that command (`skaffold` is already configured there):
```shell
$ skaffold dev --port-forward
```
