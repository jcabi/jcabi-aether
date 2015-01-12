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

import org.sonatype.aether.repository.Proxy;

/**
 * Parameter holder for org.sonatype.aether.repository.Authentication.
 *
 * @author Mauricio Herrera (oruam85@gmail.com)
 * @version $Id$
 */
public final class RepositoryProxy {

    /**
     * The type of the proxy.
     */
    private final String type;

    /**
     * The host of the proxy.
     */
    private final String host;

    /**
     * The port of the proxy.
     */
    private final int port;

    /**
     * The authentication to use for the proxy connection.
     */
    private final RepositoryAuthentication auth;

    /**
     * Creates a new proxy with the specified properties.
     * @param proxy The proxy object.
     */
    public RepositoryProxy(final Proxy proxy) {
        this.type = proxy.getType();
        this.host = proxy.getHost();
        this.port = proxy.getPort();
        this.auth = new RepositoryAuthentication(proxy.getAuthentication());
    }

    /**
     * Getter of the type attribute.
     * @return The type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Getter of the host attribute.
     * @return The host.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Getter of the port attribute.
     * @return The port.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Getter of the auth attribute.
     * @return The auth.
     */
    public RepositoryAuthentication getAuth() {
        return this.auth;
    }
}