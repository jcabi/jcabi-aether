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

import com.jcabi.aspects.Immutable;
import org.sonatype.aether.repository.Authentication;

/**
 * Parameter holder for org.sonatype.aether.repository.Authentication.
 *
 * @author Mauricio Herrera (oruam85@gmail.com)
 * @version $Id$
 */
public final class RepositoryAuthentication {

    /**
     * The user name.
     */
    private final String username;

    /**
     * The password.
     */
    @Immutable.Array
    private final char[] password;

    /**
     * The path to the private key file.
     */
    private final String privatekeyfile;

    /**
     * The passphrase for the private key file.
     */
    @Immutable.Array
    private final char[] passphrase;

    /**
     * Creates a new authentication with the specified properties.
     * @param auth The authentication object.
     */
    public RepositoryAuthentication(final Authentication auth) {
        this.username = auth.getUsername();
        this.password = auth.getPassword().toCharArray();
        this.privatekeyfile = auth.getPrivateKeyFile();
        this.passphrase = auth.getPassphrase().toCharArray();
    }

    /**
     * Getter of the username attribute.
     * @return The username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Getter of the password attribute.
     * @return The password.
     */
    public char[] getPassword() {
        return this.password;
    }

    /**
     * Getter of the privateKeyFile attribute.
     * @return The privateKeyFile.
     */
    public String getPrivateKeyFile() {
        return this.privatekeyfile;
    }

    /**
     * Getter of the passphrase attribute.
     * @return The passphrase.
     */
    public char[] getPassphrase() {
        return this.passphrase;
    }
}