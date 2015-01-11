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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;

/**
 * Parameter holder for RemoteRepository.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
public final class Repository {
	/**
	 * Id of repository.
	 */
	private final transient String identifier;

	/**
	 * Repository content type.
	 */
	private final transient String type;

	/**
	 * Repository URL.
	 */
	private final transient String url;

	/**
	 * Repository release policy.
	 */
	private final transient RepositoryPolicy release;

	/**
	 * Repository snapshot policy.
	 */
	private final transient RepositoryPolicy snapshot;

	/**
	 * Proxy settings.
	 */
	private final transient RepositoryProxy proxy;

	/**
	 * Authentication settings.
	 */
	private final transient RepositoryAuthentication authentication;

	/**
	 * Collection of mirrored repositories.
	 */
	private final transient Collection<Repository> mirrored;

	/**
	 * Is this a repository manager.
	 */
	private final transient boolean manager;

	/**
	 * Constructor.
	 * @param remote Source of data.
	 */
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Repository(final RemoteRepository remote) {
        this.identifier = remote.getId();
        this.type = remote.getContentType();
        this.url = remote.getUrl();
        this.release = remote.getPolicy(false);
        this.snapshot = remote.getPolicy(true);
        if(remote.getAuthentication()!=null){
        	this.authentication = new RepositoryAuthentication(remote.getAuthentication());
        }else{
        	this.authentication = null;
        }
        if(remote.getProxy()!=null){
        	this.proxy = new RepositoryProxy(remote.getProxy().getType(), remote.getProxy().getHost(), remote.getProxy().getPort(), this.authentication);
        }else{
        	this.proxy = null;
        }
        this.manager = remote.isRepositoryManager();
        this.mirrored = new LinkedList<Repository>();
        for (final RemoteRepository mremote
            : remote.getMirroredRepositories()) {
            this.mirrored.add(new Repository(mremote));
        }
    }

	/**
	 * Get remote repository.
	 * @return Remote repository.
	 */
	public RemoteRepository remote() {
		final RemoteRepository remote = new RemoteRepository();
		remote.setId(this.identifier);
		remote.setContentType(this.type);
		remote.setUrl(this.url);
		remote.setPolicy(false, this.release);
		remote.setPolicy(true, this.snapshot);
		
		Authentication auth =null;
		if(this.authentication!=null){
			auth = new Authentication(
					this.authentication.getUsername(),
					this.authentication.getPassword(),
					this.authentication.getPrivateKeyFile(),
					this.authentication.getPassphrase());
		}
		remote.setAuthentication(auth);
		
		Proxy proxy = null;
		if(this.proxy!=null)
		{	
			new Proxy(this.proxy.getType(),this.proxy.getHost(), this.proxy.getPort(), auth);
		}
		remote.setProxy(proxy);
		remote.setRepositoryManager(this.manager);
		final List<RemoteRepository> remotes = 
			new LinkedList<RemoteRepository>();
		remote.setMirroredRepositories(remotes);
		for (final Repository repo : this.mirrored) {
			remotes.add(repo.remote());
		}
		return remote;
	}
}
