import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*;
import hudson.security.*
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.BranchSpec;
import hudson.tasks.Maven.MavenInstallation;
import hudson.tools.InstallSourceProperty;
import hudson.tools.ToolProperty;
import hudson.tools.ToolPropertyDescriptor;
import hudson.util.DescribableList;

// Variable
def instance = Jenkins.getInstance()
String id_key = "487b3c2b-cca7-4b9b-ae94-bfccb96b3e42"//UUID.randomUUID().toString()
String id_docker = "5c6ef943-0189-456e-83d4-4f7d22c56cba"
String keyfile = "/var/jenkins_home/ssh_key/id_rsa"
String userfile = "/var/jenkins_home/docker_hub/user"
String branch = "*/agonchar"
String repository = "git@gitlab.com:mortum5/epam-devops-4rd-stream.git"
String passfile = "/var/jenkins_home/docker_hub/pass"

String username = new File(userfile).getText('UTF-8')
String password = new File(passfile).getText('UTF-8')

domain = Domain.global()
store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

// Create user

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount("andrey","andrey")
instance.setSecurityRealm(hudsonRealm)
instance.save()

// Create cred
privateKey = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL,id_key,"git",new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource(keyfile),"","test")

dockerCred = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,id_docker, "docker", username, password)

store.addCredentials(domain, privateKey)
store.addCredentials(domain, dockerCred)
// Create task

def scm = new GitSCM(GitSCM.createRepoList(repository,id_key),[new BranchSpec(branch)],false, null,null, null, null);

def flowDefinition = new org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition(scm, "Jenkinsfile")

def parent = Jenkins.instance
def job = new org.jenkinsci.plugins.workflow.job.WorkflowJob(parent, "New Job")
job.definition = flowDefinition

parent.reload()

// Install Maven
