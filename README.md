# intel

[![build status](https://github.com/mtresnik/intel/actions/workflows/gradle.yml/badge.svg)](https://github.com/mtresnik/intel/actions/workflows/gradle.yml/)
[![version](https://img.shields.io/badge/version-1.0.0-blue)](https://github.com/mtresnik/intel/packages/1423052)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/mtresnik/intel/blob/main/LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-green.svg?style=flat-square)](https://makeapullrequest.com)
<hr>

## Dependencies
> Please compile against [com.resnik.math:1.0.0](https://github.com/mtresnik/math/packages/1409888) as well as this project.

## Getting Started

> This is a slightly different process to that of [com.resnik.math](https://github.com/mtresnik/math/).

**~/.m2/settings.xml:**
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
    ...
  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>
    ...
  <servers>
    <server>
      <id>github</id>
      <username>GITHUB_USERNAME</username>
      <password>GITHUB_PAT</password>
    </server>
  </servers>
</settings>
```

**pom.xml:**
```xml
<repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/mtresnik/math</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
...
<dependency>
    <groupId>com.resnik</groupId>
    <artifactId>math</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>com.resnik</groupId>
    <artifactId>intel</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle (groovy)

**~/.gradle/gradle.properties:**
```groovy
gpr.user=GITHUB_USERNAME
gpr.token=GITHUB_PAT
```

**build.gradle:**
```groovy
repositories {
    ...
    maven {
        name="GitHubPackages"
        url= uri("https://maven.pkg.github.com/mtresnik/math")
        credentials {
            // Runner stored in env, else stored in ~/.gradle/gradle.properties
            username = System.getenv("USERNAME") ?: findProperty("gpr.user") ?: "<GITHUB_USERNAME>"
            password = System.getenv("TOKEN") ?: findProperty("gpr.token")
        }
    }
    ...
    maven {
        name="GitHubPackages"
        url= uri("https://maven.pkg.github.com/mtresnik/intel")
        credentials {
            // Runner stored in env, else stored in ~/.gradle/gradle.properties
            username = System.getenv("USERNAME") ?: findProperty("gpr.user") ?: "<GITHUB_USERNAME>"
            password = System.getenv("TOKEN") ?: findProperty("gpr.token")
        }
    }
}

dependencies {
    ...
    implementation group: 'com.resnik', name: 'math', version: '1.0.0'
    implementation group: 'com.resnik', name: 'intel', version: '1.0.0'
    ...
}
```

<hr>

## Constraint Satisfaction Problems (CSP's)

Finding solutions to smaller CSP's is simple enough using recursion, but harder for larger / more complex domains. For larger domains we run into the `StackOverflowException` and `Java OEM Exception` since the solutionset is stored at each point on the stack in a recursion model.

**CSPAgent** : A non-recursive Depth-First-Search (DFS) graph explorer with a Stack of parents.
**CSPSingleParentAgent** : A `CSPAgent` with only one parent (used in Async CSP's)

### CSP Types:

* **CSPTree** : Synchronous, linear DFS on the search space using a `CSPAgent`.
* **CSPDomainAsync** : Separates the first variable's domain into |D| `CSPAgent`'s on separate sections of the search space.
* **CSPInferredAsync** : Uses `THREAD_COUNT` `CSPAgent`'s on separate sections of the search space.
* **CSPCoroutine** : Uses kotlin coroutines in JVM Thread Pools.

> If unsure which CSP type to use, just call `CSPFactory`

```kotlin
val variables = listOf<String>("A", "B", "C")
val domain = listOf<Int>(1, 2, 3)
val domainMap = variables.associateWith{ domain }
val csp = CSPFactory.createCSP(domainMap)
csp.addConstraint(GlobalAllDiff())
val solutions = csp.findAllSolutions()
val timeTaken = csp.finalTime()
```

### Constraints:

Constraints are used to limit the search space given the initial variables and domains. **Local Constraints** provide *path consistency* while **Global Constraints** provide *absolute consistency*.

**Local Constraints** imply that local consistency tends to global consistency. Used for monotone solutions. </br> Ex: `sumLessThan()`, `localAllDiff()`

**Global Constraints** imply that the entire sequence is needed to know consistency. </br> Ex: `isName()`, `equals()`

**Reusable Constraints** accomplishes both local and global consistency. These can both prune the search space while exploring and be reused as global constraints at the end. </br> Ex: `min()`, `max()`

> **_NOTE:_** High computation constraints should be weighed against search space complexity.

#### Worst Case Complexity (No Solutions, No Constraints):

`// Had to change the background on this one for dark-mode legibility`
</br>
<img src="https://i.imgur.com/i7BBTz4.png" width="300">

### Examples

#### Map Coloring

| Same Sized Rects - Trivial Case | Different Sized Rects - First Solution | 
| -------- | -------- |
| <img src="https://i.imgur.com/IfRWdAO.png" width="300">     | <img src="https://i.imgur.com/n6bo1II.png" width="300">     |

#### n-Queens

| (n=8, solutions=92, time=101ms) | (n=14, solutions=365596, time=49.435s) | 
| -------- | -------- |
| <img src="https://i.imgur.com/rzmAFtC.jpg" width="300">    | <img src="https://i.imgur.com/8WvWPvB.jpg" width="300">     |

## Quad Trees and k-d Trees

Speed Limit Sign (quad tree, distance threshold = 40 / 255.0) </br>
<img src="https://i.imgur.com/HgdFG3b.png" width="300"> </br>
Sunday in the Park With George (k-d tree, uniform random seeds = 1000) </br>
<img src="https://i.imgur.com/oK2I3az.png" width="300">

## K-Means Clustering

### Euclidean Distance Relations
K-Means Shown as Points (k=20, seedPoints=40) </br>
<img src="https://i.imgur.com/XEML1tA.png" width="300">

**Seed Points**: Sample Data the model is clustered on.

```kotlin=1
val kMeans : KMeans
// Initialize the kMeans with seed points
val closestCluster : Cluster = kMeans[samplePoint]
```

For initializing a kMeans clustering, use `KMeans.getBestKMeans()`.
This will minimize the kMeans variance over the number of iterations.

```kotlin=4
// kMeansVariance: high = poor clustering
val kMeansVariance = kMeans.getMeanVariance()
// each cluster has variance as well
val clusterVariance = closestCluster.getVariance()
```

### Image Compression

For image compression, your number of means is the number of unique colors you want. As k increases, from 8 to 16, 32, 64, etc, the quality of the image increases. Similarly, the number of seed points needed from the original image may increase when k is small to preserve information content.

Video: </br>
[![K-Means Image Compression](https://img.youtube.com/vi/4C_0dY91V1U/0.jpg)](https://www.youtube.com/watch?v=4C_0dY91V1U)

## Proof of Concept Work

### Neural Networks

#### Feed Forward Neural Network
(xor problem, training set=4, data={(0,0),(0)}, {(0,1),(1)}, {(1,0),(1)}, {(1,1),(0)}) </br>
<img src="https://i.imgur.com/xBGMrqR.png" width="300">

#### Recurrant Neural Networks / Convolutional Neural Networks (WIP)

More Tensor iteration work is needed before these are *fully fleshed out.

### Support Vector Machines (SVM's)

<img src="https://i.imgur.com/tdmBOE9.png" width="300">

### Decision Trees

Example of decision trees on the Tennis Dataset but generalized for other Attribute types.

#### Tennis Dataset
Attributes = (Outlook, Temperature, Humidity, Wind, PlayTennis) </br>
Outlook = (Sunny, Overcast, Rain) </br>
Temperature = (Hot, Mild, Cool) </br>
Humidity = (High, Normal) </br>
Wind = (Weak, Strong) </br>
PlayTennis = (No, Yes) </br>

```kotlin 
val schema = Schema(outlook, temperature, humidity, wind, tennis)
val dataset = Dataset(schema, tennis) // schema, target
dataset.addEntry(Entry(schema, sunny, hot, high, weak, no))
// ...
// Add other Entries
// ...
val decisionTree = dataset.buildTree()
val result = decisionTree.traverse("Sunny", "Mild", "High", "Strong")
// The typeof(result) is the same as the TargetAttribute.type
```
In general, you can have any number of `Attribute`'s each with different types in the same `Schema`.

### Genetic Algorithms (WIP)

Mainly used for String breeding algorithms and minimization functions.

> Note: Future work will include multi-threaded breeders and speciation in a custom bioinformatics library.
