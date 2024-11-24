# krosbridge

A Kotlin ROS Client for rosbridge to publish/subscribe/call services with an [automatic code generator](https://github.com/thoebert/krosbridge-codegen) for the required message/service data classes. 

## Features

* **Message API:** Publish/Subscribe to Message Topics
* **Service API:** Call/Advertise Services
* **ROS data classes:** Pre-generated bases classes for ROS standard messages/services e.g. `std_msg/Header`
* **Code Generation of data classes:** Use the ROS message (`.msg`) and service (`.srv`) files to automatically generate Kotlin data classes for easy usage. 
* **Automatic Serialization:** The (generated) Kotlin data classes are automatically serialized to JSON using the [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
* **Websocket HTTP Client agnostic:** Use any HTTP client for the Websocket connection. [See here.](https://ktor.io/docs/http-client-engines.html)
* **Coroutines API** The implemented Service API uses Kotlin Coroutines for async calls.

## Usage

### Setup

To setup the ROS-Bridge connection:

```kotlin
val ros = Ros("localhost")
ros.connect()
```

### Publish/Subscribe to a Message Topic

Put the following Message `Num.msg` in `src/main/ros/myrospackage/msg`. 
Make sure your custom message type is registered to your ROS-Core/ROS-Bridge project `myrosproject`. [See here how.](http://wiki.ros.org/ROS/Tutorials/CreatingMsgAndSrv)

#### Num.msg:
```
int64 num
```

To publish 10 `Num.msg` messages to the topic named `/num_topic` 

```kotlin
val topic = NumTopic(ros, "/num_topic")
topic.advertise()
for (i in 1..10) {
    topic.publish(i.toLong())
    delay(1000)
}
```

To subscribe to the topic exposed at `/num_topic`:

```kotlin
val topic = NumTopic(ros, "/num_topic")
topic.subscribe("subscriptionID") { msg, _ ->
    println("Received: ${msg.num}")
}
//delay(100_000) // don't stop execution
```

### Call/Advertise a Service

Put the following service definition `AddTwoInts.srv` in `src/main/ros/myrospackage/srv`. Make sure your custom service type is registered to your ROS-Core/ROS-Bridge project `myrosproject`. [See here how.](http://wiki.ros.org/ROS/Tutorials/CreatingMsgAndSrv)

#### AddTwoInts.srv:
```
int64 a
int64 b
---
int64 sum
```

To call the `AddTwoInts.srv` service exposed at `/add_two_ints_service`:

```kotlin
val service = AddTwoInts(ros, "/add_two_ints_service")
val (sum, result) = service.call(13, 42)
```

To advertise and expose the `AddTwoInts` service at `/add_two_ints_service`:

```kotlin
val service = AddTwoInts(ros, "/add_two_ints_service")
service.advertiseService { req, id ->
    service.sendResponse(req.a + req.b, true, id)
}
//delay(100_000) // don't stop execution
```

## Setup

The following instructions will setup your project with krosbridge.

### Gradle

Add the following lines to your `build.gradle.kts` to 
1) add the [code-gen gradle plugin](https://github.com/thoebert/krosbridge-codegen) and the gradle kotlin serialization plugin
2) add jitpack to your dependencies repository
3) add the required and optional dependencies
4) configure the code-generation package
add source set for generated sources

```kotlin
import com.github.thoebert.krosbridgecodegen.KROSBridgeCodegenPluginConfig

plugins {
    kotlin("plugin.serialization") version "1.7.21" // 1)
    id("io.github.thoebert.krosbridge-codegen") version "1.0.6"
}

...

repositories {
    mavenCentral()
    maven("https://jitpack.io") // 2) add line here or for android: add in settings.gradle.kts to dependencyResolutionManagement.repositories
}

dependencies { // 3)
    // Required
    implementation("com.github.thoebert:krosbridge:main-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("io.ktor:ktor-client-okhttp:2.2.4")
    
    // Optional for logging: 
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")
    ...
}

...

configure<KROSBridgeCodegenPluginConfig> { // 4)
    packageName.set("com.company.project.messages") // package for generated data classes
}

...

// only for kotlin
kotlin.sourceSets {
    named("main"){
        kotlin.srcDir("build/generated/source/ros")
    }
}
// only on android
android.sourceSets {
    named("main") {
        java.srcDirs(File(buildDir, "generated/source/ros"))
    }
}
```

### Build

Run the `generateROSSources` gradle task to generate all messages/services in the folder `/build/generated/source/ros/com/company/project/messages/myrospackage/` 
```shell
./gradlew generateROSSources
```

Import them using:

```kotlin
import com.company.project.messages.myrospackage.Num
import com.company.project.messages.myrospackage.NumTopic
import com.company.project.messages.myrospackage.AddTwoInts
```

### Debugging

To see debug messages, set the minimum log level to `debug` for the package `com.github.thoebert.krosbridge`.
Use any SLF4J-Logging facade implementation, e.g. log4j. [See here.](https://www.slf4j.org/manual.html#swapping)

## Contributing

Feel free to open a new issue/pull-request about any possible improvement.

## Author

* [Timon Höbert](https://github.com/thoebert)
* [Russell Toris](https://github.com/rctoris)

This project is originally based on [jrosbridge](https://github.com/rctoris/jrosbridge).

## License

This project is licensed under the BSD - see the [License](License) file for details.
