<img src="http://img.jcabi.com/logo-square.svg" width="64px" height="64px" />

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![Managed by Zerocracy](https://www.0crat.com/badge/C3RUBL5H9.svg)](https://www.0crat.com/p/C3RUBL5H9)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-aether)](http://www.rultor.com/p/jcabi/jcabi-aether)

[![Build Status](https://travis-ci.org/jcabi/jcabi-aether.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-aether)
[![PDD status](http://www.0pdd.com/svg?name=jcabi/jcabi-aether)](http://www.0pdd.com/p?name=jcabi/jcabi-aether)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-aether/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-aether)
[![Javadoc](https://javadoc.io/badge/com.jcabi/jcabi-aether.svg)](http://www.javadoc.io/doc/com.jcabi/jcabi-aether)
[![Dependencies](https://www.versioneye.com/user/projects/561ac2e2a193340f32001011/badge.svg?style=flat)](https://www.versioneye.com/user/projects/561ac2e2a193340f32001011)

[![jpeek report](http://i.jpeek.org/com.jcabi/jcabi-aether/badge.svg)](http://i.jpeek.org/com.jcabi/jcabi-aether/)

Aether adapter for Maven plugins

More details are here: [aether.jcabi.com](http://aether.jcabi.com/index.html)

[Aether](http://www.eclipse.org/aether/) is a dependency
management toolkit for Maven repositories. It is very convenient to use Aether
in your Maven plugins, when it's necessary to find a location of certain
artifact or find out what transitive dependencies it contains. This module
contains `Aether` class, an adapter between your plugin and Aether.

This is how you find out the location of a `junit:junit-dep:4.10` artifact:

```java
import com.jcabi.aether.Aether;
import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

public class MyMojo extends AbstractMojo {
  /**
   * @parameter default-value="${repositorySystemSession}"
   * @readonly
   */
  private RepositorySystemSession session;
  @Override
  public void execute() {
    File repo = this.session.getLocalRepository().getBasedir();
    Collection<Artifact> deps = new Aether(this.getProject(), repo).resolve(
      new DefaultArtifact("junit", "junit-dep", "", "jar", "4.10"),
      JavaScopes.COMPILE
    );
    // Now you have a full set of artifacts that include junit-dep.jar
    // and all its dependencies in "runtime" scope. The first
    // element in the collection is junit-dep.jar itself. You can use
    // Artifact#getFile() method to get its absolute path
  }
}
```

## Questions?

If you have any questions about the framework, or something doesn't work as expected,
please [submit an issue here](https://github.com/jcabi/jcabi-aether/issues/new).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```
