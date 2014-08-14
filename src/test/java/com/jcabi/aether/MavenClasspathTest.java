/**
 * Copyright (c) 2012-2013, JCabi.com
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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * Test case for {@link com.jcabi.aether.MavenClasspath}.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
@SuppressWarnings("unchecked")
public final class MavenClasspathTest {

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
        final Dependency dep = new Dependency();
        final String group = "junit";
        dep.setGroupId(group);
        dep.setArtifactId(group);
        dep.setVersion("4.10");
        dep.setScope("test");
        final String jar = "junit-4.10.jar";
        final DependencyGraphBuilder builder = this.builder(jar);
        final MavenSession session = Mockito.mock(MavenSession.class);
        final MavenProject project = this.project(dep);
        Mockito.when(session.getCurrentProject()).thenReturn(project);
        MatcherAssert.assertThat(
            new MavenClasspath(builder, session, MavenClasspath.TEST_SCOPE),
            Matchers.<File>hasItems(
                Matchers.hasToString(
                    Matchers.endsWith(
                        String.format(
                            "%sas%<sdirectory",
                            System.getProperty("file.separator")
                        )
                    )
                ),
                Matchers.hasToString(Matchers.endsWith(jar))
            )
        );
    }

    /**
     * Classpath can return a string when a dependency is broken.
     * @throws Exception If there is some problem inside
     */
    @Test
    @Ignore
    public void hasToStringWithBrokenDependency() throws Exception {
        final Dependency dep = new Dependency();
        dep.setGroupId("junit-broken");
        dep.setArtifactId("junit-absent");
        dep.setVersion("1.0");
        dep.setScope(MavenClasspath.TEST_SCOPE);
        final DependencyGraphBuilder builder =
            Mockito.mock(DependencyGraphBuilder.class);
        final MavenSession session = Mockito.mock(MavenSession.class);
        final MavenProject project = this.project(dep);
        Mockito.when(session.getCurrentProject()).thenReturn(project);
        final MavenClasspath classpath = new MavenClasspath(
            builder, session, MavenClasspath.TEST_SCOPE
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
        dep.setScope(MavenClasspath.COMPILE_SCOPE);
        final DependencyGraphBuilder builder =
            Mockito.mock(DependencyGraphBuilder.class);
        final MavenSession session = Mockito.mock(MavenSession.class);
        final MavenProject project = this.project(dep);
        Mockito.when(session.getCurrentProject()).thenReturn(project);
        final MavenClasspath classpath = new MavenClasspath(
            builder, session, MavenClasspath.TEST_SCOPE
        );
        MatcherAssert.assertThat(classpath, Matchers.equalTo(classpath));
        MatcherAssert.assertThat(
            classpath.canEqual(classpath),
            Matchers.is(true)
        );
    }

    /**
     * Build DependencyGraphBuilder with single dependency node.
     * @param location Location of node jar.
     * @return Container.
     */
    private DependencyGraphBuilder builder(final String location) {
        final DependencyGraphBuilder builder = Mockito
            .mock(DependencyGraphBuilder.class);
        final DependencyNode node = Mockito.mock(DependencyNode.class);
        try {
            Mockito.when(
                builder.buildDependencyGraph(
                    Mockito.any(MavenProject.class),
                    Mockito.any(ArtifactFilter.class)
                )
            )
                .thenReturn(node);
        } catch (final DependencyGraphBuilderException ex) {
            throw new IllegalStateException(ex);
        }
        final Artifact artifact = Mockito.mock(Artifact.class);
        Mockito.when(artifact.getFile()).thenReturn(new File(location));
        Mockito.when(node.getArtifact()).thenReturn(artifact);
        return builder;
    }

    /**
     * Creates project with this dependency.
     * @param dep Dependency to add to the project
     * @return Maven project mocked
     * @throws Exception If there is some problem inside
     */
    private MavenProject project(final Dependency dep) throws Exception {
        final MavenProject project = Mockito.mock(MavenProject.class);
        Mockito.doReturn(Arrays.asList("/some/path/as/directory"))
            .when(project).getTestClasspathElements();
        Mockito.doReturn(Arrays.asList(dep)).when(project).getDependencies();
        return project;
    }

}
