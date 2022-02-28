plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-data"))
    api(project(":pleo-antaeus-models"))
    implementation ("com.rabbitmq:amqp-client:5.9.0")
    implementation("dev.inmo:krontab:0.7.0")
}