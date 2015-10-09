/**
 * Copyright (c) 2012-2015, jcabi.com
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
package com.jcabi;

import com.jcabi.aether.Aether;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * Test case for {@link com.jcabi.aether.Aether}.
 * Test proxy congiguration.
 * @author Shelan Perera (shelanrc@gmail.com)
 * @version $Id$
 */
public class AetherTest {
    /**
     * Temp dir.
     *
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public final transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Aether go through proxy configuration.
     * @throws Exception If something goes wrong.
     */
    @Test(expected = DependencyResolutionException.class)
    public final void serverTest() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
                .next(new MkAnswer.Simple(""))
                .start(8002);
        System.setProperty(
                "org.apache.maven.user-settings",
                getClass().getResource("/settings.xml").getPath()
        );
        final Collection<RemoteRepository> remotes = Arrays.asList(
            new RemoteRepository(
                    "central",
                    "default",
                    "http://repo1.maven.org/maven2/"
            )
        );
        final File local = this.temp.newFolder();
        new Aether(remotes, local)
            .resolve(
                    new DefaultArtifact(
                            "junit",
                            "junit-dep",
                            "", "jar", "4.10"
                ),
                    "runtime"
        );
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
                query.headers().get("Proxy-Authorization"),
                Matchers.hasItem("Basic cHJveHl1c2VyOnByb3h5cGFzcw==")
        );
    }

}
