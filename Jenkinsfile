// Minor housekeeping logic
boolean specialBranch = env.BRANCH_NAME.equals("master") || env.BRANCH_NAME.equals("develop")

// String to use in a property that determines artifact pruning (has to be a String not a number)
String artifactBuildsToKeep = "1"
if (specialBranch) {
    artifactBuildsToKeep = "10"
}

properties([
    // Needed due to the Copy Artifact plugin deciding to implement an obnoxious security feature that can't simply be turned off
    copyArtifactPermission('*'),
    // Flag for Jenkins to discard attached artifacts after x builds
    buildDiscarder(logRotator(artifactNumToKeepStr: artifactBuildsToKeep))
])

/**
 * Main pipeline definition for building the engine.
 *
 * It uses the Scripted Pipeline Syntax.
 * See https://www.jenkins.io/doc/book/pipeline/#declarative-versus-scripted-pipeline-syntax
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
node ("ts-engine && heavy && java8") {
    stage('Setup') {
        echo "Going to check out the things !"
        checkout scm
        sh 'chmod +x gradlew'
    }

    stage('Build') {
        // Jenkins sometimes doesn't run Gradle automatically in plain console mode, so make it explicit
        sh './gradlew --console=plain clean extractConfig extractNatives distForLauncher testDist'
        archiveArtifacts 'gradlew, gradle/wrapper/*, templates/build.gradle, config/**, facades/PC/build/distributions/Terasology.zip, engine/build/resources/main/org/terasology/engine/version/versionInfo.properties, natives/**, build-logic/src/**, build-logic/*.kts'
    }

    stage('Unit Tests') {
        try {
            sh './gradlew --console=plain unitTest'
        } finally {
            // Gradle generates both a HTML report of the unit tests to `build/reports/tests/*` and XML reports
            // to `build/test-results/*`.
            // We need to upload the XML reports for visualization in Jenkins. 
            //
            // See https://docs.gradle.org/current/userguide/java_testing.html#test_reporting
            junit testResults: '**/build/test-results/unitTest/*.xml'
        }
    }

    stage('Publish') {
        if (specialBranch) {
            withCredentials([usernamePassword(credentialsId: 'artifactory-gooey', usernameVariable: 'artifactoryUser', passwordVariable: 'artifactoryPass')]) {
                sh './gradlew --console=plain -Dorg.gradle.internal.publish.checksums.insecure=true publish -PmavenUser=${artifactoryUser} -PmavenPass=${artifactoryPass}'
            }
        } else {
            println "Running on a branch other than 'master' or 'develop' bypassing publishing"
        }

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

    stage('Analytics') {
        sh './gradlew --console=plain check -x test'
        // the default resolution when omitting `defaultBranch` is to `master` - which is wrong in our case. 
        discoverGitReferenceBuild(defaultBranch: 'develop') //TODO: does this also work for PRs with different base branch?
        recordIssues skipBlames: true,
            tools: [
                checkStyle(pattern: '**/build/reports/checkstyle/*.xml'),
                spotBugs(pattern: '**/build/reports/spotbugs/main/*.xml', useRankAsPriority: true),
                pmdParser(pattern: '**/build/reports/pmd/*.xml')
            ] 
            
        recordIssues skipBlames: true, 
            tool: taskScanner(includePattern: '**/*.java,**/*.groovy,**/*.gradle', lowTags: 'WIBNIF', normalTags: 'TODO', highTags: 'ASAP')
    }

    stage('Documentation') {
        sh './gradlew --console=plain javadoc'
        step([$class: 'JavadocArchiver', javadocDir: 'engine/build/docs/javadoc', keepAll: false])
        recordIssues skipBlames: true, tool: javaDoc()
    }

    stage('Integration Tests') {
        try {
            sh './gradlew --console=plain integrationTest'
        } finally {
            // Gradle generates both a HTML report of the unit tests to `build/reports/tests/*` and XML reports
            // to `build/test-results/*`.
            // We need to upload the XML reports for visualization in Jenkins. 
            //
            // See https://docs.gradle.org/current/userguide/java_testing.html#test_reporting
            junit testResults: '**/build/test-results/integrationTest/*.xml', allowEmptyResults: true
        }
    }
}
