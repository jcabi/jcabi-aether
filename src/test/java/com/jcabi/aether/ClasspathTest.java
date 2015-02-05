/**
 * Copyright (c) 2012-2014, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.aether;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.JavaScopes;

/**
 * Test case for {@link Classpath}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("unchecked")
public final class ClasspathTest {

    /**
     * Group of test artifact.
     */
    private static final String GROUP = "junit";

    /**
     * Temp dir.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public final transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Classpath can build a classpath.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsClasspath() throws Exception {
        MatcherAssert.assertThat(
            new Classpath(
                this.project(
                    this.dependency(
                        ClasspathTest.GROUP, ClasspathTest.GROUP, "4.10"
                    )
                ), this.temp.newFolder(), JavaScopes.TEST
            ),
            Matchers.<File>hasItems(
                Matchers.hasToString(
                    Matchers.endsWith(
                        String.format(
                            // @checkstyle MultipleStringLiterals (2 lines)
                            "%sas%<sdirectory",
                            System.getProperty("file.separator")
                        )
                    )
                ),
                Matchers.hasToString(Matchers.endsWith("junit-4.10.jar")),
                Matchers.hasToString(Matchers.endsWith("hamcrest-core-1.1.jar"))
            )
        );
    }

    /**
     * Classpath should return artifact with highest version number.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void returnsArtifactWithHighestVersionNumber() throws Exception {
        MatcherAssert.assertThat(
            new Classpath(
                this.project(
                    this.dependency(
                        ClasspathTest.GROUP, ClasspathTest.GROUP, "4.10"
                    ),
                    this.dependency(
                        ClasspathTest.GROUP, ClasspathTest.GROUP, "4.8"
                    ),
                    this.dependency(
                        ClasspathTest.GROUP, ClasspathTest.GROUP, "4.8.2"
                    )
                ), this.temp.newFolder(), JavaScopes.TEST
            ),
            Matchers.<File>hasItems(
                Matchers.hasToString(
                    Matchers.endsWith(
                        String.format(
                            // @checkstyle MultipleStringLiterals (2 lines)
                            "%sas%<sdirectory",
                            System.getProperty("file.separator")
                        )
                    )
                ),
                Matchers.hasToString(Matchers.endsWith("junit-4.10.jar"))
            )
        );
    }

    /**
     * Classpath can build a classpath without optional dependencies.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsClasspathWithoutOptionalArtifacts() throws Exception {
        final Dependency dep = new Dependency();
        // @checkstyle MultipleStringLiterals (2 lines)
        dep.setGroupId("commons-validator");
        dep.setArtifactId("commons-validator");
        dep.setVersion("1.3.1");
        dep.setScope(JavaScopes.COMPILE);
        MatcherAssert.assertThat(
            new Classpath(
                this.project(dep), this.temp.newFolder(), JavaScopes.COMPILE
            ),
            Matchers.not(
                Matchers.<File>hasItems(
                    Matchers.hasToString(
                        Matchers.endsWith("oro-2.0.8.jar")
                    )
                )
            )
        );
    }

    /**
     * Classpath can return a string when a dependency is broken.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void hasToStringWithBrokenDependency() throws Exception {
        final Dependency dep = new Dependency();
        dep.setGroupId("junit-broken");
        dep.setArtifactId("junit-absent");
        dep.setVersion("1.0");
        dep.setScope(JavaScopes.TEST);
        final Classpath classpath = new Classpath(
            this.project(dep), this.temp.newFolder(), JavaScopes.TEST
        );
        MatcherAssert.assertThat(
            classpath.toString(),
            Matchers.containsString(
                "failed to load 'junit-broken:junit-absent:jar:1.0 (compile)'"
            )
        );
    }

    /**
     * Classpath can be compared to another classpath.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void comparesToAnotherClasspath() throws Exception {
        final Dependency dep = new Dependency();
        dep.setGroupId("org.apache.commons");
        dep.setArtifactId("commons-lang3-absent");
        dep.setVersion("3.0");
        dep.setScope(JavaScopes.COMPILE);
        final Classpath classpath = new Classpath(
            this.project(dep), this.temp.newFolder(), JavaScopes.TEST
        );
        MatcherAssert.assertThat(classpath, Matchers.equalTo(classpath));
        MatcherAssert.assertThat(
            classpath.canEqual(classpath),
            Matchers.is(true)
        );
    }

    /**
     * Create test dependency.
     * @param group Dependency group
     * @param artifact Dependency artifact ID
     * @param version Dependency Version
     * @return Created dependency.
     */
    private Dependency dependency(final String group, final String artifact,
        final String version) {
        final Dependency dep = new Dependency();
        dep.setGroupId(group);
        dep.setArtifactId(artifact);
        dep.setVersion(version);
        dep.setScope(JavaScopes.TEST);
        return dep;
    }

    /**
     * Creates project with this dependency.
     * @param dep Dependency to add to the project
     * @return Maven project mocked
     * @throws Exception If there is some problem inside
     */
    private MavenProject project(final Dependency... dep) throws Exception {
        final MavenProject project = Mockito.mock(MavenProject.class);
        Mockito.doReturn(Arrays.asList("/some/path/as/directory"))
            .when(project).getTestClasspathElements();
        Mockito.doReturn(Arrays.asList(dep)).when(project).getDependencies();
        final List<RemoteRepository> repos = Arrays.asList(
            new RemoteRepository(
                "maven-central",
                "default",
                "http://repo1.maven.org/maven2/"
            )
        );
        Mockito.doReturn(repos).when(project).getRemoteProjectRepositories();
        return project;
    }

}
