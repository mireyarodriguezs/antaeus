plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    api(project(":pleo-antaeus-core"))
    implementation("dev.inmo:krontab:0.7.0")
}