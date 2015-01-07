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
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.version.GenericVersionScheme;
import org.sonatype.aether.version.InvalidVersionSpecificationException;
import org.sonatype.aether.version.VersionScheme;

/**
 * A classpath of a Maven Project.
 *
 * <p>It is a convenient wrapper around {@link Aether} class, that allows you
 * to fetch all dependencies of a Maven Project by their scope. The class
 * implements a {@link Set} of {@link File}s and can be used like this:
 *
 * <pre> String classpath = StringUtils.join(
 *   new Classpath(project, localRepo, "runtime")
 *   System.getProperty("path.separator")
 * );</pre>
 *
 * <p>Important to notice that this class resolves artifacts from repositories
 * only once per instance. It means that once resolved the list of files
 * is cached and never flushes. In order to resolve again (if you think that
 * content of repositories is changed), make a new instance of the class.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.16
 * @see Aether
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@EqualsAndHashCode(callSuper = false, of = { "project", "aether", "scopes" })
@Loggable(
    value = Loggable.DEBUG,
    limit = 1, unit = TimeUnit.MINUTES,
    trim = false
)
@SuppressWarnings("PMD.TooManyMethods")
public final class Classpath extends AbstractSet<File> {

    /**
     * Maven Project.
     */
    private final transient MavenProject project;

    /**
     * Aether to work with.
     */
    private final transient Aether aether;

    /**
     * Artifact scopes to include.
     */
    private final transient Set<String> scopes;

    /**
     * Public ctor.
     * @param prj The Maven project
     * @param repo Local repository location (directory path)
     * @param scp The scope to use, e.g. "runtime" or "compile"
     */
    public Classpath(@NotNull final MavenProject prj,
        @NotNull final File repo, @NotNull final String scp) {
        this(prj, repo, Arrays.asList(scp));
    }

    /**
     * Public ctor.
     * @param prj The Maven project
     * @param repo Local repository location (directory path)
     * @param scps All scopes to include
     */
    public Classpath(@NotNull final MavenProject prj,
        @NotNull final File repo, @NotNull final Collection<String> scps) {
        super();
        this.project = prj;
        this.aether = new Aether(prj, repo);
        this.scopes = new HashSet<String>(scps);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return StringUtils.join(this.roots(), "\n");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<File> iterator() {
        try {
            return this.fetch().iterator();
        } catch (final DependencyResolutionException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        try {
            return this.fetch().size();
        } catch (final DependencyResolutionException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Fetch all files found (JAR, ZIP, directories, etc).
     * @return Set of files
     * @throws DependencyResolutionException If can't resolve
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<File> fetch() throws DependencyResolutionException {
        final Set<File> files = new LinkedHashSet<File>(0);
        for (final String path : this.elements()) {
            files.add(new File(path));
        }
        for (final Artifact artifact : this.artifacts()) {
            files.add(artifact.getFile());
        }
        return files;
    }

    /**
     * Get Maven Project elements.
     * @return Collection of them
     */
    private Collection<String> elements() {
        final Collection<String> elements = new LinkedList<String>();
        try {
            if (this.scopes.contains(JavaScopes.TEST)) {
                elements.addAll(this.project.getTestClasspathElements());
            }
            if (this.scopes.contains(JavaScopes.RUNTIME)) {
                elements.addAll(this.project.getRuntimeClasspathElements());
            }
            if (this.scopes.contains(JavaScopes.SYSTEM)) {
                elements.addAll(this.project.getSystemClasspathElements());
            }
            if (this.scopes.contains(JavaScopes.COMPILE)
                || this.scopes.contains(JavaScopes.PROVIDED)) {
                elements.addAll(this.project.getCompileClasspathElements());
            }
        } catch (final DependencyResolutionRequiredException ex) {
            throw new IllegalStateException("Failed to read classpath", ex);
        }
        return elements;
    }

    /**
     * Set of unique artifacts, which should be available in classpath.
     *
     * <p>This method gets a full list of artifacts of the project,
     * including their transitive dependencies.
     *
     * @return The set of artifacts
     * @throws DependencyResolutionException If can't resolve some of them
     */
    private Set<Artifact> artifacts() throws DependencyResolutionException {
        final Set<Artifact> artifacts = new LinkedHashSet<Artifact>(0);
        for (final RootArtifact root : this.roots()) {
            for (final Artifact child : root.children()) {
                if (Classpath.contains(child, artifacts)) {
                    final Artifact found = Classpath.find(child, artifacts);
                    if (found.getVersion().equals(child.getVersion())) {
                        continue;
                    }
                    final Artifact newer = Classpath.newer(child, found);
                    if (newer.equals(child)) {
                        artifacts.remove(found);
                        artifacts.add(newer);
                    }
                }
                if (root.excluded(child)) {
                    continue;
                }
                artifacts.add(child);
            }
        }
        return artifacts;
    }

    /**
     * Find which artifact has newer version.
     * @param child One of the artifacts to compare.
     * @param found Second artifact to compare.
     * @return Newer artifact of the two provided.
     */
    private static Artifact newer(final Artifact child, final Artifact found) {
        final VersionScheme scheme = new GenericVersionScheme();
        final Artifact newer;
        try {
            if (scheme.parseVersion(child.getVersion())
                .compareTo(scheme.parseVersion(found.getVersion())) < 0) {
                newer = found;
            } else {
                newer = child;
            }
        } catch (final InvalidVersionSpecificationException ex) {
            throw new IllegalStateException(ex);
        }
        return newer;
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
    private Set<RootArtifact> roots() {
        final Set<RootArtifact> roots = new LinkedHashSet<RootArtifact>(0);
        for (final Dependency dep : this.project.getDependencies()) {
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
    private RootArtifact root(final Dependency dep) {
        return new RootArtifact(
            this.aether,
            new DefaultArtifact(
                dep.getGroupId(),
                dep.getArtifactId(),
                dep.getClassifier(),
                dep.getType(),
                dep.getVersion()
            ),
            dep.getExclusions()
        );
    }

    /**
     * Artifact exists in collection?
     * @param artifact The artifact
     * @param artifacts Collection of them
     * @return TRUE if it is already there
     */
    private static boolean contains(final Artifact artifact,
        final Collection<Artifact> artifacts) {
        boolean contains = false;
        for (final Artifact exists : artifacts) {
            if (artifact.getArtifactId().equals(exists.getArtifactId())
                && artifact.getGroupId().equals(exists.getGroupId())
                && artifact.getClassifier().equals(exists.getClassifier())) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    /**
     * Find artifact in collection.
     * Artifact has to exist in the collection otherwise
     * IllegalArgumentException will be thrown.
     *
     * @param artifact The artifact
     * @param artifacts Collection of them
     * @return Found artifact,
     */
    private static Artifact find(final Artifact artifact,
        final Collection<Artifact> artifacts) {
        for (final Artifact exists : artifacts) {
            if (artifact.getArtifactId().equals(exists.getArtifactId())
                && artifact.getGroupId().equals(exists.getGroupId())
                && artifact.getClassifier().equals(exists.getClassifier())) {
                return exists;
            }
        }
        throw new IllegalArgumentException("Artifact not found");
    }
}

