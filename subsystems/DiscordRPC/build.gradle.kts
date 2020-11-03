plugins {
    java
    `java-library`
}

apply(from = "$rootDir/config/gradle/publish.gradle")

dependencies {
    implementation(project(":engine"))
    api("com.jagrosh:DiscordIPC:0.4")
    implementation("ch.qos.logback:logback-classic:1.2.3")
}