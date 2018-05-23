FROM jenkins/jenkins:lts

USER root

RUN apt-get update \
  && apt-get -y install sudo apt-transport-https ca-certificates curl gnupg2 software-properties-common \
  && curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add - \
  && add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/debian $(lsb_release -cs) stable" \
  && apt-get update && apt-get -y --no-install-recommends install docker-ce \
  && rm -rf /var/lib/apt/lists/* \
  && rm -rf /var/cache/apt/* \
  && echo "jenkins ALL=NOPASSWD: ALL" >> /etc/sudoers

USER jenkins

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY custom.groovy /usr/share/jenkins/ref/init.groovy.d/custom.groovy
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
