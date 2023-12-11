subprojects {
    dependencies {
        compileOnly(project(":api"))
        compileOnly(project(":dist"))
    }
}