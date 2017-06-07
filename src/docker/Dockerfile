# Copyright (c) 2012-2017, jcabi.com
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met: 1) Redistributions of source code must retain the above
# copyright notice, this list of conditions and the following
# disclaimer. 2) Redistributions in binary form must reproduce the above
# copyright notice, this list of conditions and the following
# disclaimer in the documentation and/or other materials provided
# with the distribution. 3) Neither the name of the rultor.com nor
# the names of its contributors may be used to endorse or promote
# products derived from this software without specific prior written
# permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
# NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
# FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
# THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
# INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
# STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
# OF THE POSSIBILITY OF SUCH DAMAGE.

FROM ubuntu:14.04
MAINTAINER Yegor Bugayenko <yegor256@gmail.com>
LABEL Description="This is the image for aether.jcabi.com" Vendor="jcabi.com" Version="1.0"
WORKDIR /tmp

ENV DEBIAN_FRONTEND=noninteractive
ENV OPENJDK_VERSION 7u111-2.6.7-0ubuntu0.14.04.3
ENV MAVEN_VERSION 3.3.9

RUN apt-get update && apt-get install -y software-properties-common \
    && rm -rf /var/lib/apt/lists/* \
    && apt-add-repository ppa:brightbox/ruby-ng

RUN apt-get update && apt-get install -y \
    wget bcrypt curl \
    unzip zip \
    gnupg gnupg2 \
    jq \
    bsdmainutils \
    libxml2-utils \
    build-essential \
    automake autoconf \
    git \
    s3cmd \
    libmagic-dev=1:5.14-2ubuntu3.3 \
    zlib1g-dev=1:1.2.8.dfsg-1ubuntu1 \
    ruby2.2 \
    ruby2.2-dev \
    openjdk-7-jdk="${OPENJDK_VERSION}" \
    && rm -rf /var/lib/apt/lists/*

RUN gem update && gem install \
    nokogiri:1.6.7.2 \
    bundler:1.11.2

# Maven
RUN mkdir -p /usr/share/maven \
    && curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
    && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven

# Clean up
RUN rm -rf /tmp/*
RUN rm -rf /root/.ssh
