buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.1") // 최신 안정 버전으로 변경
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21") // 최신 Kotlin 버전
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
