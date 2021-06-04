<div align="center">

# forge4multimc

A library to programatically generate a MultiMC instance from a legacy Forge installer
</div>

## How to use as a developer
Call
```java
Installers.getInstaller(yourJarHere).install(multiMcDirectory, yourJarHere);
```

## How to use as a user
Head over [here](https://github.com/ChachyDev/multimc4forge/releases) and download the latest jar from the assets' area.
Once you've downloaded the jar either execute the jar manually or if you would like to be in a CLI mode or are in a headless
state, manually execute the jar from the Terminal/Command Prompt via java -jar /path/to/jar --directory /path/to/multimc --jar /path/to/forge/installer.jar
