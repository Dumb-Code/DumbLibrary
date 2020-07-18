[![Build Status](https://travis-ci.org/Dumb-Code/DumbLibrary.svg?branch=master)](https://travis-ci.org/Dumb-Code/DumbLibrary)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=net.dumbcode.dumblibrary&metric=alert_status)](https://sonarcloud.io/dashboard?id=net.dumbcode.dumblibrary)

# DumbLibrary

A Minecraft modding library made for DumbCode mods. It builds off of the library: LLibrary (See Below), and includes animation among other useful tools.

## Documentation
[Entity Component System](https://github.com/Dumb-Code/DumbLibrary/wiki/Entity-Component-System)  
[Animation System](https://github.com/Dumb-Code/DumbLibrary/wiki/Animation-API)

### Contributing

First, make a fork of this repository. When you've done that, you can clone that repository. Depending on your installation settings, you either have to run the command in the git Bash, or the CMD/Terminal.

Say what the step will be

```
git clone https://github.com/<Your Username>/DumbLibrary.git
```

When that's done, go into the newly created directory `DumbLibrary` and run in the console if you're on Windows:
```
gradlew.bat setupDecompWorkspace
```
Or this when you're on any other operating system (Like Mac OS X and Ubuntu):
```
./gradlew setupDecompWorkspace
```

Then for Eclipse:
```
gradlew.bat eclipse
```

or for IntelliJ IDEA:
```
gradlew.bat idea
```

## Built With

* [Gradle](https://gradle.org/) - Dependency Management
* [Travis-CI](https://travis-ci.org/) - Continuous Integration
* [SonarCloud](https://sonarcloud.io) - Continous Inspection

## License

This project is licensed under the GNU Lesser General Public License v3.0 - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* [LLibrary](https://minecraft.curseforge.com/projects/llibrary) - Library
