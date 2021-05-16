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

// Main pipeline definition
node ("ts-engine && heavy && java8") {
    stage('Checkout') {
        echo "Going to check out the things !"
        checkout scm
        sh 'chmod +x gradlew'
    }
    stage('Build') {
        // Jenkins sometimes doesn't run Gradle automatically in plain console mode, so make it explicit
        sh './gradlew --console=plain clean extractConfig extractNatives distForLauncher testDist'
        archiveArtifacts 'gradlew, gradle/wrapper/*, templates/build.gradle, config/**, facades/PC/build/distributions/Terasology.zip, engine/build/resources/main/org/terasology/engine/version/versionInfo.properties, natives/**, build-logic/src/**, build-logic/*.kts'
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
        sh "./gradlew --console=plain check spotbugsmain javadoc"
    }
    stage('Record') {
        junit testResults: '**/build/test-results/test/*.xml',  allowEmptyResults: true
        recordIssues tool: javaDoc()
        step([$class: 'JavadocArchiver', javadocDir: 'engine/build/docs/javadoc', keepAll: false])
        recordIssues tool: checkStyle(pattern: '**/build/reports/checkstyle/*.xml')
        recordIssues tool: spotBugs(pattern: '**/build/reports/spotbugs/main/*.xml', useRankAsPriority: true)
        recordIssues tool: pmdParser(pattern: '**/build/reports/pmd/*.xml')
        recordIssues tool: taskScanner(includePattern: '**/*.java,**/*.groovy,**/*.gradle', lowTags: 'WIBNIF', normalTags: 'TODO', highTags: 'ASAP')
    }
}
