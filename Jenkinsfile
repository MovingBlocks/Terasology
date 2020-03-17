node ("default-java") {
    stage('Checkout') {
        echo "Going to check out the things !"
        checkout scm
        sh 'chmod +x gradlew'
    }
    stage('Build') {
        sh './gradlew clean distForLauncher'
        archiveArtifacts 'gradlew, gradle/wrapper/*, modules/Core/build.gradle, config/**, facades/PC/build/distributions/Terasology.zip, build/resources/main/org/terasology/version/versionInfo.properties, natives/**'
    }
    stage('Analytics') {
        sh "./gradlew --console=plain check"
    }
    stage('Publish') {
        withCredentials([usernamePassword(credentialsId: 'artifactory-gooey', usernameVariable: 'artifactoryUser', passwordVariable: 'artifactoryPass')]) {
            sh './gradlew publish -PmavenUser=${artifactoryUser} -PmavenPass=${artifactoryPass}'
        }
    }
    stage('Record') {
        junit testResults: 'build/test-results/test/*.xml'
        recordIssues tool: javaDoc()
        step([$class: 'JavadocArchiver', javadocDir: 'engine/build/docs/javadoc', keepAll: false])
        recordIssues tool: checkStyle(pattern: 'build/reports/checkstyle/*.xml')
        recordIssues tool: spotBugs(pattern: '**/build/reports/spotbugs/*.xml', useRankAsPriority: true)
        recordIssues tool: pmdParser(pattern: '**/reports/pmd/*.xml')
        recordIssues tool: taskScanner(includePattern: '**/*.java,**/*.groovy,**/*.gradle', lowTags: 'WIBNIF', normalTags: 'TODO', highTags: 'ASAP')
    }
}
