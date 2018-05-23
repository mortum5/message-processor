import groovy.json.JsonSlurper

pipeline {
    agent any
    environment {
        username = "mortum5"
        repository = "git@gitlab.com:mortum5/epam-devops-4rd-stream.git"
        branch = "agonchar"
        credIdGit = "487b3c2b-cca7-4b9b-ae94-bfccb96b3e42"
        credIdDocker = "5c6ef943-0189-456e-83d4-4f7d22c56cba"
        requestUrl = "https://requestbin.fullcontact.com"
        requestBinId = ""
        logs = "New Jobs\n"
    }

    triggers {
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }

    stages {
        stage('Preparation') {
            steps {
                sh 'sudo chown :1000 /var/run/docker.sock'
                deleteDir()
            }
        }

        stage('Git') {
            steps {
                git branch: "$branch", changelog: false, credentialsId: "$credIdGit", poll: false, url: "$repository"
            }
        }

        stage('Test') {
            agent {
                docker {
                    image 'maven:3.5'
                }
            }
            steps {
                script {
                    sh "mvn clean test"
                }
            }
        }

        stage('Build') {
            agent {
                docker {
                    image 'maven:3.5'
                }
            }
            steps {
                script {
                    sh "mvn clean package -Dmaven.test.skip=true"
                }
            }
        }


        stage('Build images') {
            steps {
                script {
                    sh 'docker build message-processor -t "$username"/message-processor:"$BUILD_NUMBER"'
                    sh 'docker build message-gateway -t "$username"/message-gateway:"$BUILD_NUMBER"'
                }
            }
        }

        stage('Push images') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: "$credIdDocker",
                                passwordVariable: 'PASSWORD',
                                usernameVariable: 'USERNAME')]) {
                        sh("docker login --username=$USERNAME --password=$PASSWORD")
                        sh("docker push $USERNAME/message-processor:$BUILD_NUMBER")
                        sh("docker push $USERNAME/message-gateway:$BUILD_NUMBER")
                    }
                }
            }
        }

        stage('Create request bin') {
            steps {
                script {
                    def response = httpRequest (
                            consoleLogResponseBody: true,
                            httpMode: 'POST',
                            url: "${request_url}/api/v1/bins",
                            validResponseCodes: '100:503'
                            ).getContent()
                    requestBinId = new JsonSlurper().parseText(response).name.toString()
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    sh 'docker run -d --name message-gateway -p 8888:8080 "$username"/message-gateway:"$BUILD_NUMBER"'
                    sh 'docker run -d --name rabbitmq --net=container:message-gateway rabbitmq'
                    sleep 3
                    sh 'docker run -d --name message-processor --net=container:message-gateway "$username"/message-processor:"$BUILD_NUMBER"'
                    sleep 20
                    timeout(time: 60, unit: 'SECONDS') {
                        waitUntil {
                            try {
                                def var = sh (script:"docker ps -q -f status=exited -f name=message-processor", returnStdout: true)
                                    if (var != "") {
                                        sh 'docker start message-processor'
                                        sleep 3
                                        throw new Exception()
                                    } else {
                                        return true
                                    }
                            } catch (exception) {
                                return false
                            }
                        }
                    }
                }
            }
        }

        stage('IntegrationTest') {
            steps {
                script {
                    sleep 3
                    def tests = ['curl http://localhost:8080/message -X POST -d \'{"messageId":1, "timestamp":1234, "protocolVersion":"1.0.0", "messageData":{"mMX":1234, "mPermGen":1234}}\'',
                                'curl http://localhost:8080/message -X POST -d \'{"messageId":2, "timestamp":2234, "protocolVersion":"1.0.1", "messageData":{"mMX":1234, "mPermGen":5678, "mOldGen":22222}}\'',
                                'curl http://localhost:8080/message -X POST -d \'{"messageId":3, "timestamp":3234, "protocolVersion":"2.0.0", "payload":{"mMX":1234, "mPermGen":5678, "mOldGen":22222, "mYoungGen":333333}}\'']
                    tests.eachWithIndex { test, index ->
                        timeout(time: 60, unit: 'SECONDS') {
                            waitUntil {
                                try {
                                    def t = sh(script : "docker exec message-gateway ${test}", returnStdout: true)
                                    if (t == "OK" ) {
                                        return true
                                    } else {
                                        sleep 3
                                        throw new Exception()
                                    }
                                } catch (exception) {
                                    return false
                                }
                            }
                        }
                        def log = sh(returnStdout : true, script : "docker logs --tail 1 message-processor")
                        logs += (log + "\n")

                    }
                }
            }
        }

        stage('Publish report') {
            steps {
                httpRequest( consoleLogResponseBody: true,
                    httpMode: 'POST',
                    url: "${requestUrl}/${requestBinId}",
                    requestBody: "$logs")
                echo "!!!!!!!!!!!!!\nSee report at ${requestUrl}/${requestBinId}?inspect\n!!!!!!!!!!!!!"
            }
        }
    }

    post {
        always {
            sh "docker stop rabbitmq"
            sh "docker rm rabbitmq"
            sh "docker stop message-processor"
            sh "docker rm message-processor"
            sh "docker stop message-gateway"
            sh "docker rm message-gateway"
        }
    }
}
