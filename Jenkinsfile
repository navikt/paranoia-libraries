#!/usr/bin/env groovy
@Library('gjenlevende-pipeline') _

node {
    def appToken
    def commitHash
    try {
        cleanWs()

        stage("checkout") {
            appToken = github.generateAppToken()

            sh "git init"
            sh "git pull https://x-access-token:$appToken@github.com/navikt/paranoia-libraries.git"

            commitHash = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
            github.commitStatus("pending", "navikt/paranoia-libraries", appToken, commitHash)
        }

        stage("build") {
            def mvnHome = tool "maven-3.5.3"
            withEnv(["PATH+MAVEN=${mvnHome}/bin"]) {
                sh "mvn -B -V verify"
            }
        }

        stage("deploy") {
            def mvnHome = tool "maven-3.5.3"
            withEnv(["PATH+MAVEN=${mvnHome}/bin"]) {
                sh "mvn -B -V -fn deploy -DskipTests -Dmaven.install.skip=true"
            }
        }

        github.commitStatus("success", "navikt/paranoia-libraries", appToken, commitHash)
    } catch (err) {
        github.commitStatus("failure", "navikt/paranoia-libraries", appToken, commitHash)

        slackSend([
                color  : 'danger',
                message: "Build <${env.BUILD_URL}|#${env.BUILD_NUMBER}> failed (paranoia-libraries)"
        ])

        throw err
    }
}