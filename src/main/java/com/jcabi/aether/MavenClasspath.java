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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.File;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;

/**
 * A classpath of a Maven Project.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@EqualsAndHashCode(callSuper = false, of = { "builder", "scopes" })
@Loggable(
    value = Loggable.DEBUG,
    limit = 1, unit = TimeUnit.MINUTES,
    trim = false
)
public final class MavenClasspath extends AbstractSet<File> {

    /**
     * Maven test scope.
     */
    public static final String TEST_SCOPE = "test";

    /**
     * Maven runtime scope.
     */
    public static final String RUNTIME_SCOPE = "runtime";

    /**
     * Maven system scope.
     */
    public static final String SYSTEM_SCOPE = "system";

    /**
     * Maven compile scope.
     */
    public static final String COMPILE_SCOPE = "compile";

    /**
     * Maven provided scope.
     */
    public static final String PROVIDED_SCOPE = "provided";

    /**
     * Artifact scopes to include.
     */
    private final transient Set<String> scopes;

    /**
     * Dependency graph builder.
     */
    private final transient DependencyGraphBuilder builder;

    /**
     * The current repository/network configuration of Maven.
     */
    private final transient MavenSession session;

    /**
     * Public ctor.
     * @param bldr Dependency graph builder.
     * @param sess Maven session.
     * @param scp The scope to use, e.g. "runtime" or "compile"
     */
    public MavenClasspath(@NotNull final DependencyGraphBuilder bldr,
        @NotNull final MavenSession sess,
        @NotNull final String scp) {
        this(bldr, sess, Arrays.asList(scp));
    }

    /**
     * Public ctor.
     * @param bldr Dependency graph builder.
     * @param sess Maven session.
     * @param scps All scopes to include
     */
    public MavenClasspath(@NotNull final DependencyGraphBuilder bldr,
        @NotNull final MavenSession sess,
        @NotNull final Collection<String> scps) {
        super();
        this.builder = bldr;
        this.session = sess;
        this.scopes = new HashSet<String>(scps);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        for (final Dependency dep
            : this.session.getCurrentProject().getDependencies()) {
            if (this.scopes.contains(dep.getScope())) {
                try {
                    text.append(this.root(dep));
                    // @checkstyle IllegalCatch (1 line)
                } catch (final Exception ex) {
                    text.append(
                        Logger.format(
                            "failed to load '%s:%s:%s:%s (%s)' %s",
                            dep.getGroupId(),
                            dep.getArtifactId(),
                            dep.getType(),
                            dep.getVersion(),
                            dep.getScope(),
                            ex.getMessage()
                        )
                    );
                }
                text.append("\n");
            }
        }
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<File> iterator() {
        return this.fetch().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.fetch().size();
    }

    /**
     * Fetch all files found (JAR, ZIP, directories, etc).
     * @return Set of files
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<File> fetch() {
        final Set<File> files = new LinkedHashSet<File>(0);
        for (final String path : this.elements()) {
            files.add(new File(path));
        }
        try {
            files.addAll(this.dependencies(this.graph(), this.scopes));
        } catch (final DependencyGraphBuilderException ex) {
            throw new IllegalStateException(ex);
        }
        return files;
    }

    /**
     * Build dependency graph.
     * @return Root of dependency graph.
     * @throws DependencyGraphBuilderException In case of error.
     */
    private DependencyNode graph() throws DependencyGraphBuilderException {
        return this.builder.buildDependencyGraph(
            this.session.getCurrentProject(),
            new ArtifactFilter() {
                @Override
                public boolean include(
                    final Artifact artifact) {
                    return MavenClasspath.this.scopes
                        .contains(artifact.getScope());
                }
            }
        );
    }

    /**
     * Convert dependencies to root artifacts.
     *
     * <p>The method is getting a list of artifacts from Maven Project, without
     * their transitive dependencies (that's why they are called "root"
     * artifacts).
     *
     * @return The set of root artifacts
     */
    private Set<MavenRootArtifact> roots() {
        final Set<MavenRootArtifact> roots =
            new LinkedHashSet<MavenRootArtifact>(0);
        for (final Dependency dep
            : this.session.getCurrentProject().getDependencies()) {
            if (!this.scopes.contains(dep.getScope())) {
                continue;
            }
            roots.add(this.root(dep));
        }
        return roots;
    }

    /**
     * Convert dependency to root artifact.
     * @param dep Dependency
     * @return Root artifact
     */
    private MavenRootArtifact root(final Dependency dep) {
        final DefaultArtifact artifact = new DefaultArtifact(
            dep.getGroupId(),
            dep.getArtifactId(),
            dep.getVersion(),
            dep.getScope(),
            dep.getType(),
            dep.getClassifier(),
            new DefaultArtifactHandler()
        );
        try {
            final Collection<Artifact> children = new LinkedList<Artifact>();
            for (final DependencyNode child : this.graph().getChildren()) {
                children.add(child.getArtifact());
            }
            return new MavenRootArtifact(
                artifact,
                dep.getExclusions(),
                children
            );
        } catch (final DependencyGraphBuilderException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Get Maven Project elements.
     * @return Collection of them
     */
    private Collection<String> elements() {
        final Collection<String> elements = new LinkedList<String>();
        try {
            if (this.scopes.contains(TEST_SCOPE)) {
                elements.addAll(
                    this.session.getCurrentProject().getTestClasspathElements()
                );
            }
            if (this.scopes.contains(RUNTIME_SCOPE)) {
                elements.addAll(
                    this.session.getCurrentProject()
                        .getRuntimeClasspathElements()
                );
            }
            if (this.scopes.contains(SYSTEM_SCOPE)) {
                elements.addAll(
                    this.session.getCurrentProject()
                        .getSystemClasspathElements()
                );
            }
            if (this.scopes.contains(COMPILE_SCOPE)
                || this.scopes.contains(PROVIDED_SCOPE)) {
                elements.addAll(
                    this.session.getCurrentProject()
                        .getCompileClasspathElements()
                );
            }
        } catch (final DependencyResolutionRequiredException ex) {
            throw new IllegalStateException("Failed to read classpath", ex);
        }
        return elements;
    }

    /**
     * Retrieve dependencies for from given node and scope.
     * @param node Node to traverse.
     * @param scps Scopes to use.
     * @return Collection of dependency files.
     */
    private Collection<File> dependencies(final DependencyNode node,
        final Collection<String> scps) {
        final Artifact artifact = node.getArtifact();
        final Collection<File> files = new LinkedList<File>();
        if ((artifact.getScope() == null)
            || scps.contains(artifact.getScope())) {
            if (artifact.getScope() == null) {
                files.add(artifact.getFile());
            } else {
                files.add(
                    this.session.getLocalRepository().find(artifact).getFile()
                );
            }
            for (final DependencyNode child : node.getChildren()) {
                if (child.getArtifact().compareTo(node.getArtifact()) != 0) {
                    files.addAll(this.dependencies(child, scps));
                }
            }
        }
        return files;
    }
}

