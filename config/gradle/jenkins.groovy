// TODO: Verify that a build.gradle included via Git is overwritten by Jenkins

// Find the true name of the project - probably available within Jenkins but this way can test easier outside
def settingsGradleFile = new File('settings.gradle')
def moduleName = settingsGradleFile.getAbsoluteFile().parentFile.name - "Nano"
println "Preparing settings.gradle for module '$moduleName'"

// Make sure no existing file exists, no sneaking in stealthy Gradle stuff (would have to be in Git, Jenkins cleans thoroughly)
settingsGradleFile.delete()

def settingsGradleContent = """
// This allows us to have jobs in Jenkins with names that differ from their module name (such as jobs for "develop" and "master")
rootProject.name = '$moduleName'

// Experimental flag to feed Gradle in a release build job situation. Could be elsewhere in Jenkins but this is nice and easy
def isReleaseBuild = true
"""
	
settingsGradleFile << settingsGradleContent
