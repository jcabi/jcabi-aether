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

import com.jcabi.aspects.Cacheable;
import com.jcabi.log.Logger;
import java.util.Collection;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Exclusion;

/**
 * One root artifact found in the project.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.16
 */
@EqualsAndHashCode(of = { "art", "exclusions" })
final class MavenRootArtifact {

    /**
     * The artifact.
     */
    @NotNull
    private final transient Artifact art;

    /**
     * Exclusions.
     */
    @NotNull
    private final transient Collection<Exclusion> exclusions;

    /**
     * This artifact child artifacts.
     */
    @NotNull
    private final transient Collection<Artifact> chldrn;

    /**
     * Ctor.
     * @param artifact The artifact
     * @param excl Exclusions
     * @param chld Child artifacts
     */
    protected MavenRootArtifact(@NotNull final Artifact artifact,
        @NotNull final List<Exclusion> excl,
        @NotNull final Collection<Artifact> chld) {
        this.art = artifact;
        this.exclusions = excl;
        this.chldrn = chld;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append(
            Logger.format(
                "%s:%s:%s:%d",
                this.art.getGroupId(),
                this.art.getArtifactId(),
                this.art.getVersion(),
                this.exclusions.size()
            )
        );
        for (final Artifact child : this.children()) {
            text.append("\n  ").append(child);
            if (this.excluded(child)) {
                text.append(" (excluded)");
            }
        }
        return text.toString();
    }

    /**
     * Get artifact.
     * @return The artifact
     */
    public Artifact artifact() {
        return this.art;
    }

    /**
     * Get all dependencies of this root artifact.
     * @return The list of artifacts
     */
    @Cacheable(forever = true)
    @SuppressWarnings("unchecked")
    public Collection<Artifact> children() {
        return this.chldrn;
    }

    /**
     * Is this one should be excluded?
     * @param artifact The artifact to check
     * @return TRUE if it should be excluded
     */
    public boolean excluded(@NotNull final Artifact artifact) {
        boolean excluded = false;
        for (final Exclusion exclusion : this.exclusions) {
            if (exclusion.getArtifactId().equals(artifact.getArtifactId())
                && exclusion.getGroupId().equals(artifact.getGroupId())) {
                excluded = true;
                break;
            }
        }
        return excluded;
    }

}
