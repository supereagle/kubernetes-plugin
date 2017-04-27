jenkins-kubernetes-plugin
=========================

> This plugin has been customized with following features:
> - Support container slave pool
> - Support CPU/memory request/limit
> - Support node selector
> - Support volume sharing
> - Support filtering failed container slaves
>
> **`Note`**:
> - Most of above features have been implemented in the latest version, except the most important feature `Support container slave pool`.
> - This plugin has stably run in production for 2+ years, on [Jenkins](https://github.com/kohsuke/jenkins) v1.599 and v2.7.2, [Kubernetes](https://github.com/kubernetes/kubernetes) v1.1.4 and v1.2.4.

Jenkins plugin to run dynamic slaves in a Kubernetes/Docker environment.

Based on the [Scaling Docker with Kubernetes](http://www.infoq.com/articles/scaling-docker-with-kubernetes) article,
automates the scaling of Jenkins slaves running in Kubernetes.

The plugin creates a Kubernetes Pod for each slave started,
defined by the Docker image to run, and stops it after each build.

Slaves are launched using JNLP, so it is expected that the image connects automatically to the Jenkins master.
For that some environment variables are automatically injected:

* `JENKINS_URL`: Jenkins web interface url
* `JENKINS_JNLP_URL`: url for the jnlp definition of the specific slave
* `JENKINS_SECRET`: the secret key for authentication

Tested with [`csanchez/jenkins-slave`](https://registry.hub.docker.com/u/csanchez/jenkins-slave/),
see the [Docker image source code](https://github.com/carlossg/jenkins-slave-docker).


# Configuration on Google Container Engine

Create a cluster 
```
    gcloud beta container clusters create jenkins --num-nodes 1 --machine-type g1-small
```
and note the admin password and server certitifate.

Or use Google Developer Console to create a Container Engine cluster, then run 
```
    gcloud beta container get-credentials
    kubectl config view --raw
```
the last command will output kubernetes cluster configuration including API server URL, admin password and root certificate

# Debugging

To inspect the json messages sent back and forth to the Kubernetes API server you can configure
a new [Jenkins log recorder](https://wiki.jenkins-ci.org/display/JENKINS/Logging) for `org.apache.http`
at `DEBUG` level.


# Building

Run `mvn clean package` and copy `target/kubernetes.hpi` to Jenkins plugins folder.

# Docker image

Docker image for Jenkins, with plugin installed.
Based on the [official image](https://registry.hub.docker.com/_/jenkins/).

## Running

    docker run --rm --name jenkins -p 8080:8080 -p 50000:50000 -v /var/jenkins_home csanchez/jenkins-kubernetes

## Testing locally

A local testing cluster with one node can be created with Docker Compose

    docker-compose up

When using boot2docker or Docker Engine with a remote host, the remote Kubernetes API can be exposed
with `docker-machine ssh MACHINE_NAME -- -L 0.0.0.0:8080:localhost:8080` or `boot2docker ssh -L 0.0.0.0:8080:localhost:8080`

More info

* [Docker CookBook examples](https://github.com/how2dock/docbook/tree/master/ch05/docker)
* [Kubernetes Getting started with Docker](https://github.com/GoogleCloudPlatform/kubernetes/blob/master/docs/getting-started-guides/docker.md)

## Running in Kubernetes (Google Container Engine)

Assuming you created a Kubernetes cluster named `jenkins` this is how to run both Jenkins and slaves there.

Create a GCE disk named `kubernetes-jenkins` to store the data, and format it as ext4.
Formatting is not needed in new versions of Kubernetes.

Creating the pods and services

    gcloud preview container pods create --config-file ./src/main/kubernetes/pod.yml
    gcloud preview container services create --config-file ./src/main/kubernetes/service-http.yml
    gcloud preview container services create --config-file ./src/main/kubernetes/service-slave.yml

Open the firewall to the Jenkins master running in a pod

    gcloud compute firewall-rules create jenkins-node-master --allow=tcp:8888 --target-tags k8s-jenkins-node

Connect to the ip of the network load balancer created by Kubernetes, port 8888. Get the ip with

    gcloud compute forwarding-rules describe jenkins

Configure Jenkins, adding the `Kubernetes` cloud under configuration, setting
Kubernetes URL to the container engine cluster endpoint, and the correct username and password.
Set Container Cap to a reasonable number for tests, i.e. 3.

Add an image with

* ID: `csanchez/jenkins-slave`
* Remote filesystem root: `/home/jenkins`
* Remote FS Root Mapping: `/home/jenkins`

![image](configuration.png)

Now it is ready to be used.

Tearing it down

    gcloud preview container pods delete jenkins
    gcloud preview container services delete jenkins
    gcloud preview container services delete jenkins-slave



## Building

    docker build -t csanchez/jenkins-kubernetes .
