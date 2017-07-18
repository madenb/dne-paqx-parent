UPSTREAM_JOBS_LIST = [
    "vce-symphony/common-dependencies/${env.BRANCH_NAME}",
    "vce-symphony/common-client-parent/${env.BRANCH_NAME}",
    "vce-symphony/hdp-capability-registry-client/${env.BRANCH_NAME}",
    "vce-symphony/common-messaging-parent/${env.BRANCH_NAME}",
    "dellemc-symphony/engineering-standards-service-parent/${env.BRANCH_NAME}",
    "dellemc-symphony/compute-capabilities-api/${env.BRANCH_NAME}",
    "dellemc-symphony/virtualization-capabilities-api/${env.BRANCH_NAME}"
]
UPSTREAM_JOBS = UPSTREAM_JOBS_LIST.join(',')

MAVEN_PHASE = "install"
if (env.BRANCH_NAME ==~ /master|develop|release\/.*/) {
    MAVEN_PHASE = "deploy"
}

pipeline { 
    parameters {
        string(name: 'dockerImagesDel', defaultValue: 'true')
        string(name: 'dockerRegistry',  defaultValue: 'docker-dev-local.art.local')
        string(name: 'dockerImageTag',  defaultValue: '${BRANCH_NAME}.${BUILD_NUMBER}')
    }
    triggers {
        upstream(upstreamProjects: UPSTREAM_JOBS, threshold: hudson.model.Result.SUCCESS)
    }
    agent {
        node {
            label 'maven-builder'
            customWorkspace "workspace/${env.JOB_NAME}"
        }
    }
    environment {
        GITHUB_TOKEN = credentials('github-02')
    }
    options { 
        buildDiscarder(logRotator(artifactDaysToKeepStr: '30', artifactNumToKeepStr: '5', daysToKeepStr: '30', numToKeepStr: '5'))
        timestamps()
        disableConcurrentBuilds()
    }
    tools {
        maven 'linux-maven-3.3.9'
        jdk 'linux-jdk1.8.0_102'
    }
    stages {
        stage("Build") {
            steps {
                sh "mvn clean ${MAVEN_PHASE} -Dmaven.repo.local=.repo -DskipDocker=false -PbuildDockerImageOnJenkins -Ddocker.registry=${params.dockerRegistry} -DdockerImage.tag=${params.dockerImageTag} -DdeleteDockerImages=${params.dockerImagesDel}"
            }
        }
        stage('Record Test Results') {
            steps {
                junit '**/target/*-reports/*.xml'
            }
        }
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: '**/*.rpm', fingerprint: true 
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') { 
                    doSonarAnalysis()    
                }
            }
        }
        stage('Third Party Audit') {
            steps {
                doThirdPartyAudit()
            }
        }
        stage('PasswordScan') {
		    steps {
			    doPwScan()
		    }
	    }
        stage('Github Release') {
            steps {
                githubRelease()
            }
        }
        stage('NexB Scan') {
            steps {
                sh 'rm -rf .repo'
                doNexbScanning()
            }
        }        
    }
    post {
        always{
            cleanWorkspace()   
        }
        success {
            emailext attachLog: true, 
                body: 'Pipeline job ${JOB_NAME} success. Build URL: ${BUILD_URL}', 
                recipientProviders: [[$class: 'CulpritsRecipientProvider']], 
                subject: 'SUCCESS: Jenkins Job- ${JOB_NAME} Build No- ${BUILD_NUMBER}', 
                to: 'pebuildrelease@vce.com'            
        }
        failure {
            emailext attachLog: true, 
                body: 'Pipeline job ${JOB_NAME} failed. Build URL: ${BUILD_URL}', 
                recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'DevelopersRecipientProvider'], [$class: 'FailingTestSuspectsRecipientProvider'], [$class: 'UpstreamComitterRecipientProvider']], 
                subject: 'FAILED: Jenkins Job- ${JOB_NAME} Build No- ${BUILD_NUMBER}', 
                to: 'pebuildrelease@vce.com'
        }
    }
}
