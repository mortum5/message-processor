#!/bin/bash

USERNAME="mortum5" # username docker hub
PASSWORD="" # password docker hub

cd k8s/jenkins
docker build . -t "$USERNAME"/jenkins:lts
docker login
docker push "$USERNAME"/jenkins:lts
cd ../
echo -n "$USERNAME" > user
echo -n "$PASSWORD" > pass
cp ~/.ssh/id_rsa ./
kubectl create -f jenkins-namespace.yaml;
kubectl -n jenkins create secret generic ssh-key-gitlab --from-file=id_rsa=./id_rsa;
kubectl -n jenkins create secret generic docker-hub --from-file=user=./user --from-file=pass=./pass
kubectl create -f creds.yaml;
kubectl create -f jenkins.yaml;
kubectl create -f service_jenkins.yaml;
