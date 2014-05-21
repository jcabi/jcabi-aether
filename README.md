<img src="http://img.jcabi.com/logo-square.png" width="64px" height="64px" />
 
[![Build Status](https://travis-ci.org/jcabi/jcabi-aether.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-aether)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-aether/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-aether)

Aether adapter for Maven plugins

More details are here: [www.jcabi.com/jcabi-aether](http://www.jcabi.com/jcabi-aether/index.html)

[Aether](https://docs.sonatype.org/display/AETHER/Home) is a dependency
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
please [submit an issue here](https://github.com/yegor256/jcabi/issues/new).
If you want to discuss, please use our [Google Group](https://groups.google.com/forum/#!forum/jcabi).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```
