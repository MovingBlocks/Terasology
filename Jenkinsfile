node ("default-java") {
    stage('Checkout') {
        echo "Going to check out the things !"
        checkout scm
        sh 'chmod +x gradlew'
    }
    stage('Build') {
        // Jenkins sometimes doesn't run Gradle automatically in plain console mode, so make it explicit
        sh './gradlew --console=plain clean extractConfig extractNatives distForLauncher'
        archiveArtifacts 'gradlew, gradle/wrapper/*, modules/Core/build.gradle, config/**, facades/PC/build/distributions/Terasology.zip, build/resources/main/org/terasology/version/versionInfo.properties, natives/**'
    }
    stage('Publish') {
        if (env.BRANCH_NAME.equals("master") || env.BRANCH_NAME.equals("develop")) {
            withCredentials([usernamePassword(credentialsId: 'artifactory-gooey', usernameVariable: 'artifactoryUser', passwordVariable: 'artifactoryPass')]) {
                sh './gradlew --console=plain -Dorg.gradle.internal.publish.checksums.insecure=true publish -PmavenUser=${artifactoryUser} -PmavenPass=${artifactoryPass}'
            }
        } else {
            println "Running on a branch other than 'master' or 'develop' bypassing publishing"
        }
    }
    stage('Analytics') {
        sh "./gradlew --console=plain check javadoc"
    }
    stage('Record') {
        junit testResults: '**/build/test-results/test/*.xml',  allowEmptyResults: true
        recordIssues tool: javaDoc()
        step([$class: 'JavadocArchiver', javadocDir: 'engine/build/docs/javadoc', keepAll: false])
        recordIssues tool: checkStyle(pattern: '**/build/reports/checkstyle/*.xml')
        recordIssues tool: spotBugs(pattern: '**/build/reports/spotbugs/*.xml', useRankAsPriority: true)
        recordIssues tool: pmdParser(pattern: '**/build/reports/pmd/*.xml')
        recordIssues tool: taskScanner(includePattern: '**/*.java,**/*.groovy,**/*.gradle', lowTags: 'WIBNIF', normalTags: 'TODO', highTags: 'ASAP')
    }
}
