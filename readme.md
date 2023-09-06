## Running Vert.x Microservices on Kubernetes/OpenShift

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
