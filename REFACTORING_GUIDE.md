# Refactoring Guide: Engine → Three-Module Structure

**Goal:** Split current engine into `model-lib`, `engine`, and `editor` modules.

**Status:** Pre-Implementation Planning Document

---

## Current Structure

```
JBsAdventureEngine/
├── pom.xml
├── src/main/kotlin/net/daddldiddl/jbsadventure/
│   ├── Main.kt
│   ├── Game.kt
│   ├── model/
│   │   ├── GameData.kt
│   │   ├── Room.kt
│   │   ├── Item.kt
│   │   ├── Container.kt
│   │   ├── Exit.kt
│   │   ├── State.kt
│   │   ├── Name.kt
│   │   ├── OpenLockEnabledEntity.kt
│   │   ├── FixedLocations.kt
│   │   ├── SaveState.kt
│   │   └── actions/
│   │       ├── Action.kt
│   │       ├── Precondition.kt
│   │       └── ItemUsage.kt
│   ├── lang/
│   │   ├── LanguageData.kt
│   │   └── Keys.kt
│   └── tools/
│       ├── serializers/
│       ├── ConsoleOutput.kt
│       ├── SimpleFileLog.kt
│       ├── SaveManager.kt
│       ├── Config.kt
│       ├── DataValidator.kt
│       └── GameLoader.kt
└── src/main/resources/
    ├── data.json
    └── lang/en.json
```

---

## Target Structure

```
JBsAdventureEngine/
├── pom.xml                    # Parent POM
├── model-lib/                 # NEW MODULE
│   ├── pom.xml
│   └── src/main/kotlin/net/daddldiddl/jbsadventure/
│       ├── model/             # MOVED from engine
│       ├── lang/              # MOVED from engine
│       └── tools/
│           ├── serializers/   # MOVED from engine
│           ├── DataValidator.kt   # MOVED from engine
│           └── GameLoader.kt      # MOVED from engine
├── engine/                    # REFACTORED MODULE
│   ├── pom.xml               # Updated: depends on model-lib
│   ├── src/main/kotlin/net/daddldiddl/jbsadventure/
│   │   ├── Main.kt           # KEPT
│   │   ├── Game.kt           # KEPT
│   │   └── tools/
│   │       ├── ConsoleOutput.kt  # KEPT
│   │       ├── SimpleFileLog.kt  # KEPT
│   │       ├── SaveManager.kt    # KEPT (uses model-lib)
│   │       └── Config.kt         # KEPT
│   └── src/main/resources/
│       ├── data.json         # KEPT
│       └── lang/en.json      # KEPT
└── editor/                    # NEW MODULE
    ├── build.gradle.kts      # Gradle, depends on model-lib JAR
    └── src/main/kotlin/...   # Editor code
```

---

## Step-by-Step Refactoring

### Phase 1: Create Parent POM

**File:** `pom.xml` (root, rename existing)

```bash
# Backup current pom.xml
cp pom.xml engine/pom.xml

# Create new parent POM
cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>net.daddldiddl</groupId>
    <artifactId>jbs-adventure-parent</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>JB's Adventure Engine - Parent</name>

    <modules>
        <module>model-lib</module>
        <module>engine</module>
    </modules>

    <properties>
        <kotlin.version>2.3.21</kotlin.version>
        <dokka.version>2.2.0</dokka.version>
        <java.version>21</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <serialization.version>1.11.0</serialization.version>
    </properties>
</project>
EOF
```

### Phase 2: Create model-lib Module

```bash
# Create directory structure
mkdir -p model-lib/src/main/kotlin/net/daddldiddl/jbsadventure

# Move model classes
mv src/main/kotlin/net/daddldiddl/jbsadventure/model model-lib/src/main/kotlin/net/daddldiddl/jbsadventure/

# Move lang classes
mv src/main/kotlin/net/daddldiddl/jbsadventure/lang model-lib/src/main/kotlin/net/daddldiddl/jbsadventure/

# Move serializers
mkdir -p model-lib/src/main/kotlin/net/daddldiddl/jbsadventure/tools
mv src/main/kotlin/net/daddldiddl/jbsadventure/tools/serializers model-lib/src/main/kotlin/net/daddldiddl/jbsadventure/tools/

# Move DataValidator and GameLoader
mv src/main/kotlin/net/daddldiddl/jbsadventure/tools/DataValidator.kt model-lib/src/main/kotlin/net/daddldiddl/jbsadventure/tools/
mv src/main/kotlin/net/daddldiddl/jbsadventure/tools/GameLoader.kt model-lib/src/main/kotlin/net/daddldiddl/jbsadventure/tools/
```

**File:** `model-lib/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>net.daddldiddl</groupId>
        <artifactId>jbs-adventure-parent</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>jbs-adventure-model</artifactId>
    <packaging>jar</packaging>

    <name>JB's Adventure Engine - Model Library</name>
    <description>Shared data models and serializers</description>

    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlinx</groupId>
            <artifactId>kotlinx-serialization-json</artifactId>
            <version>${serialization.version}</version>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>src/main/kotlin</sourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <configuration>
                    <jvmTarget>${java.version}</jvmTarget>
                    <compilerPlugins>
                        <plugin>kotlinx-serialization</plugin>
                    </compilerPlugins>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>org.jetbrains.kotlin</groupId>
                        <artifactId>kotlin-maven-serialization</artifactId>
                        <version>${kotlin.version}</version>
                    </dependency>
                </dependencies>
                <executions>
                    <execution>
                        <id>compile</id>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Phase 3: Refactor engine Module

```bash
# Rename src to engine/src
mkdir -p engine
mv src engine/
mv target engine/ 2>/dev/null || true

# Move resources
mv engine/src/main/resources engine/src/main/
```

**File:** `engine/pom.xml` (update existing backup)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>net.daddldiddl</groupId>
        <artifactId>jbs-adventure-parent</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>jbs-adventure-engine</artifactId>
    <packaging>jar</packaging>

    <name>JB's Adventure Engine</name>
    <description>Text-based adventure game engine</description>

    <dependencies>
        <!-- Depend on model-lib -->
        <dependency>
            <groupId>net.daddldiddl</groupId>
            <artifactId>jbs-adventure-model</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>src/main/kotlin</sourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <configuration>
                    <jvmTarget>${java.version}</jvmTarget>
                </configuration>
                <executions>
                    <execution>
                        <id>compile</id>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Fat JAR with dependencies -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.8.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>net.daddldiddl.jbsadventure.MainKt</mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <profiles>
        <profile>
            <id>docs</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.jetbrains.dokka</groupId>
                        <artifactId>dokka-maven-plugin</artifactId>
                        <version>${dokka.version}</version>
                        <executions>
                            <execution>
                                <phase>package</phase>
                                <goals>
                                    <goal>dokka</goal>
                                </goals>
                            </execution>
                        </executions>
                        <configuration>
                            <outputDir>${project.build.directory}/dokka</outputDir>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
</project>
```

### Phase 4: Fix Import Statements

**Files to Update:**

1. `engine/src/main/kotlin/net/daddldiddl/jbsadventure/Main.kt`
2. `engine/src/main/kotlin/net/daddldiddl/jbsadventure/Game.kt`
3. `engine/src/main/kotlin/net/daddldiddl/jbsadventure/tools/SaveManager.kt`
4. `engine/src/main/kotlin/net/daddldiddl/jbsadventure/tools/Config.kt`

**No changes needed!** Imports remain the same:
```kotlin
import net.daddldiddl.jbsadventure.model.*
import net.daddldiddl.jbsadventure.lang.*
import net.daddldiddl.jbsadventure.tools.*
```

The package structure is preserved; only the Maven module boundary changes.

### Phase 5: Build & Test

```bash
# Clean build from root
mvn clean install

# Verify model-lib JAR created
ls -l model-lib/target/jbs-adventure-model-1.0-SNAPSHOT.jar

# Verify engine JAR created with dependencies
ls -l engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar

# Test engine still works
java -jar engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar

# Build only model-lib
mvn -pl model-lib install

# Build only engine (includes model-lib automatically)
mvn -pl engine -am package
```

### Phase 6: Create Editor Module

```bash
# Create editor directory
mkdir editor
cd editor

# Initialize Gradle project
gradle init --type kotlin-application --dsl kotlin

# Replace build.gradle.kts with Compose configuration
# (See EDITOR_DESIGN.md Section 15.1 for full content)
```

**File:** `editor/settings.gradle.kts`
```kotlin
rootProject.name = "jbs-adventure-editor"
```

**File:** `editor/build.gradle.kts`
```kotlin
// See EDITOR_DESIGN.md Section 15.1 for complete configuration
```

---

## Verification Checklist

After refactoring, verify:

- [ ] `mvn clean install` builds model-lib successfully
- [ ] `mvn -pl engine package` builds engine with fat JAR
- [ ] Engine JAR runs and plays existing adventures
- [ ] `gradle build` (in editor/) compiles without errors
- [ ] No duplicate class errors (model classes only in model-lib)
- [ ] SaveManager loads/saves games correctly
- [ ] DataValidator functions as before
- [ ] All existing unit tests pass (when added)

---

## Rollback Plan

If refactoring causes issues:

```bash
# Restore from backup (if created before refactoring)
git checkout main  # or appropriate branch

# Or manual restore:
rm -rf model-lib engine/src
git restore pom.xml src/
```

---

## Common Issues & Solutions

### Issue: "Cannot find net.daddldiddl.jbsadventure.model.*"

**Cause:** model-lib not installed to local Maven repo

**Solution:**
```bash
cd model-lib
mvn install
```

### Issue: Editor can't find model-lib JAR

**Cause:** JAR path incorrect in editor/build.gradle.kts

**Solution:**
```kotlin
// Verify path is correct relative to editor/
implementation(files("../model-lib/target/jbs-adventure-model-1.0-SNAPSHOT.jar"))
```

### Issue: Engine fat JAR missing model-lib classes

**Cause:** Assembly plugin not including transitive dependencies

**Solution:** Already configured in maven-assembly-plugin with `jar-with-dependencies`

---

## Development Workflow After Refactoring

**Typical change scenarios:**

1. **Change to model class (e.g., add field to Room):**
   ```bash
   # Edit model-lib/src/main/kotlin/.../model/Room.kt
   cd model-lib && mvn install
   cd ../engine && mvn package  # Rebuild engine
   cd ../editor && gradle build # Rebuild editor
   ```

2. **Change to engine only:**
   ```bash
   cd engine && mvn package
   ```

3. **Change to editor only:**
   ```bash
   cd editor && gradle build
   ```

4. **Full clean build:**
   ```bash
   mvn clean install  # From root, builds model + engine
   cd editor && gradle clean build
   ```

---

## Timeline Estimate

**Refactoring Duration:** 2-4 hours

- Phase 1: 15 minutes
- Phase 2: 30 minutes
- Phase 3: 30 minutes
- Phase 4: 15 minutes (likely no changes needed)
- Phase 5: 30 minutes (build + test)
- Phase 6: 1-2 hours (editor setup)

**Low Risk:** No code logic changes, only project structure reorganization.

---

## Next Steps After Refactoring

1. Commit refactored structure to Git
2. Update README.md with new build instructions
3. Begin Editor Phase 1: Core UI implementation
4. Set up CI/CD for multi-module build (optional)

---

**Document Version:** 1.0  
**Created:** 2026-06-08  
**Status:** Pre-Implementation Guide

