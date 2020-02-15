node ("default-java") {
    stage('Checkout') {
        echo "Going to check out the things !"
        checkout scm
        sh 'chmod +x gradlew'
    }
    stage('Build') {
        sh './gradlew --console=verbose clean build distPCZip'
        archiveArtifacts 'gradlew, gradle/wrapper/*, modules/Core/build.gradle, config/**, facades/PC/build/distributions/Terasology.zip, build/resources/main/org/terasology/version/versionInfo.properties, natives/**'
    }
    stage('Publish') {
        withCredentials([usernamePassword(credentialsId: 'artifactory-gooey', usernameVariable: 'artifactoryUser', passwordVariable: 'artifactoryPass')]) {
            sh './gradlew publish -PmavenUser=${artifactoryUser} -PmavenPass=${artifactoryPass}'
        }
    }
}
