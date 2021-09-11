// Minor housekeeping logic
boolean specialBranch = env.BRANCH_NAME.equals("master") || env.BRANCH_NAME.equals("develop")

// String to use in a property that determines artifact pruning (has to be a String not a number)
String artifactBuildsToKeep = "1"
if (specialBranch) {
    artifactBuildsToKeep = "10"
}

properties([
    // Needed due to the Copy Artifact plugin deciding to implement an obnoxious security feature
    // that can't simply be turned off
    copyArtifactPermission('*'),
    // Flag for Jenkins to discard attached artifacts after x builds
    buildDiscarder(logRotator(artifactNumToKeepStr: artifactBuildsToKeep))
])

/**
 * Main pipeline definition for building the engine.
 *
 * It uses the Declarative Pipeline Syntax.
 * See https://www.jenkins.io/doc/book/pipeline/syntax
 *
 * This pipeline uses Jenkins plugins to collect and report additional information about the build.
 *
 *   - Warnings Next Generation Plugin (https://plugins.jenkins.io/warnings-ng/)
 *      To record issues from code scans and static analysis tools, e.g., CheckStyle or Spotbugs.
 *   - Git Forensics Plugin (https://plugins.jenkins.io/git-forensics/)
 *      To compare code scans and static analysis against a reference build from the base branch.
 *   - JUnit Plugin (https://plugins.jenkins.io/junit/)
 *      To record the results of our test suites from JUnit format.
 *
 */

pipeline {
    agent {
        label 'ts-engine && heavy && java8'
    }
    stages {
        // declarative pipeline does `checkout scm` automatically when hitting first stage
        stage('Setup') {
            steps {
                echo 'Automatically checked out the things!'
                sh 'chmod +x gradlew'
            }
        }

        stage('Build') {
            steps {
                // Jenkins sometimes doesn't run Gradle automatically in plain console mode, so make it explicit
                sh './gradlew --console=plain clean extractConfig extractNatives distForLauncher testDist'
                archiveArtifacts '''
                    gradlew,
                    gradle/wrapper/*,
                    templates/build.gradle,
                    config/**,
                    facades/PC/build/distributions/Terasology.zip,
                    engine/build/resources/main/org/terasology/engine/version/versionInfo.properties,
                    natives/**,
                    build-logic/src/**,
                    build-logic/*.kts
                '''
            }
        }

        stage('Unit Tests') {
            steps {
                sh './gradlew --console=plain unitTest'
            }
            post {
                always {
                    // Gradle generates both a HTML report of the unit tests to `build/reports/tests/*`
                    // and XML reports to `build/test-results/*`.
                    // We need to upload the XML reports for visualization in Jenkins.
                    //
                    // See https://docs.gradle.org/current/userguide/java_testing.html#test_reporting
                    junit testResults: '**/build/test-results/unitTest/*.xml'
                }
            }
        }

        stage('Publish') {
            when {
                expression {
                    specialBranch
                }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'artifactory-gooey', \
                                                    usernameVariable: 'artifactoryUser', \
                                                    passwordVariable: 'artifactoryPass')]) {
                    sh '''./gradlew \\
                        --console=plain \\
                        -Dorg.gradle.internal.publish.checksums.insecure=true \\
                        publish \\
                        -PmavenUser=${artifactoryUser} \\
                        -PmavenPass=${artifactoryPass}
                    '''
                }
                script {
                    // Trigger the Omega dist job to repackage a game zip with modules
                    if (env.JOB_NAME.equals("Terasology/engine/develop")) {
                        build job: 'Terasology/Omega/develop', wait: false
                    } else if (env.JOB_NAME.equals("Terasology/engine/master")) {
                        build job: 'Terasology/Omega/master', wait: false
                    } else if (env.JOB_NAME.equals("Nanoware/Terasology/develop")) {
                        build job: 'Nanoware/Omega/develop', wait: false
                    } else if (env.JOB_NAME.equals("Nanoware/Terasology/master")) {
                        build job: 'Nanoware/Omega/master', wait: false
                    }
                }
            }
        }

        stage('Analytics') {
            steps {
                sh './gradlew --console=plain check -x test'
            }
            post {
                always {
                    // the default resolution when omitting `defaultBranch` is to `master`
                    // this is wrong in our case, so explicitly set `develop` as default
                    // TODO: does this also work for PRs with different base branch?
                    discoverGitReferenceBuild(defaultBranch: 'develop')
                    recordIssues(skipBlames: true, enabledForFailure: true,
                        tool: checkStyle(pattern: '**/build/reports/checkstyle/*.xml'),
                        qualityGates: [
                            [threshold: 0, type: 'NEW_HIGH', unstable: false],      // mark stage "failed" on new high findings
                            [threshold: 0, type: 'NEW_NORMAL', unstable: false],    // mark stage "failed" on new normal findings
                            [threshold: 0, type: 'TOTAL_HIGH', unstable: true],     // mark stage "unstable" on existing high findings
                            [threshold: 0, type: 'TOTAL_NORMAL', unstable: true]    // mark stage "unstable" on existing normal findings
                        ])

                    recordIssues(skipBlames: true, enabledForFailure: true,
                        tool: [
                            spotBugs(pattern: '**/build/reports/spotbugs/main/*.xml', useRankAsPriority: true),
                            pmdParser(pattern: '**/build/reports/pmd/*.xml')
                        ])

                    recordIssues(skipBlames: true, enabledForFailure: true,
                        tool: taskScanner(includePattern: '**/*.java,**/*.groovy,**/*.gradle', \
                                            lowTags: 'WIBNIF', normalTags: 'TODO', highTags: 'ASAP'))
                }
            }
        }

        stage('Documentation') {
            steps {
                sh './gradlew --console=plain javadoc'
                step([$class: 'JavadocArchiver', javadocDir: 'engine/build/docs/javadoc', keepAll: false])
                recordIssues skipBlames: true, tool: javaDoc()
            }
        }

        stage('Integration Tests') {
            steps {
                sh './gradlew --console=plain integrationTest'
            }
            post {
                always {
                    // Gradle generates both a HTML report of the unit tests to `build/reports/tests/*`
                    // and XML reports to `build/test-results/*`.
                    // We need to upload the XML reports for visualization in Jenkins.
                    //
                    // See https://docs.gradle.org/current/userguide/java_testing.html#test_reporting
                    junit testResults: '**/build/test-results/integrationTest/*.xml', allowEmptyResults: true
                }
            }
        }
    }
}
