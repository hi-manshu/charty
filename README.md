# Charty : Elementary Chart library for Compose with KMP support!

![Banner](img/banner.png)

Chart Library built using Jetpack Compose and is highly customizable. Now with Kotlin
Multiplatform (KMP) support!

_Made with ❤️ for Android Developers by Himanshu_

[![Github Followers](https://img.shields.io/github/followers/hi-manshu?label=Follow&style=social)](https://github.com/hi-manshu)
[![Twitter Follow](https://img.shields.io/twitter/follow/hi_man_shoe?label=Follow&style=social)](https://twitter.com/hi_man_shoe)
[![AndroidWeekly](https://img.shields.io/badge/Featured%20in%20androidweekly.net-%23532-orange)](https://androidweekly.net/issues/issue-532)
![GitHub issues](https://img.shields.io/github/issues/hi-manshu/charty)
![GitHub Repository size](https://img.shields.io/github/repo-size/hi-manshu/charty)
![GitHub forks](https://img.shields.io/github/forks/hi-manshu/charty)
![GitHub Repo stars](https://img.shields.io/github/stars/hi-manshu/charty)
![Charty](https://img.shields.io/maven-central/v/com.himanshoe/charty?color=f4c430&label=Maven%20Central%20%3A%20Charty)
![Charty Static Check](https://github.com/hi-manshu/charty/actions/workflows/static-check.yml/badge.svg)

````### Version Catalog

If you're using Version Catalog, you can configure the dependency by adding it to your
`libs.versions.toml` file as follows:

```toml
[versions]
#...
charty = "<version>"

[libraries]
#...
charty = { module = "com.himanshoe:charty", version.ref = "charty" }
```

### Gradle

Add the dependency below to your **module**'s `build.gradle.kts` file:

```gradle
dependencies {
    implementation("com.himanshoe:charty:$version")
    
    // if you're using Version Catalog
    implementation(libs.charty)
}
```

For Kotlin Multiplatform, add the dependency below to your commonMain source set's
`build.gradle.kts` file:

```gradle
sourceSets {
    commonMain.dependencies {
            implementation(libs.charty)
     }
}
```

Find the latest release version [here](https://github.com/hi-manshu/Charty/releases)
````
### You can find videos  iOS and Android [here](img/video)

### Photos / Screenshots

| 1                          | 2                          | 3                          | 4                          | 5                          | 6                          |
|----------------------------|----------------------------|----------------------------|----------------------------|----------------------------|----------------------------|
| ![1.png](img/photos/1.png) | ![2.png](img/photos/2.png) | ![3.png](img/photos/3.png) | ![4.png](img/photos/4.png) | ![5.png](img/photos/5.png) | ![6.png](img/photos/6.png) |

## Few apps using it:

1. [NEKO](https://github.com/nekomangaorg/Neko)
2. [TimePlanner](https://github.com/v1tzor/TimePlanner)

## Find this repository useful? :heart:
Support it by joining __[stargazers](https://github.com/hi-manshu/charty/stargazers)__ for this repository. :star: <br>

### License

```
Copyright 2025 The Charty Authors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
