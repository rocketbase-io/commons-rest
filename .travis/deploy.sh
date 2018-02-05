#!/usr/bin/env bash

set -e

# only do deployment, when travis detects a new tag
if [ ! -z "$TRAVIS_TAG" ]
then
    echo "only perform for commons-rest not for sample etc."
    cd commons-rest

    echo "on a tag -> set pom.xml <version> to $TRAVIS_TAG"
    mvn --settings ../.travis/settings.xml org.codehaus.mojo:versions-maven-plugin:2.5:set -DnewVersion=$TRAVIS_TAG -Prelease

    if [ ! -z "$TRAVIS" -a -f "$HOME/.gnupg" ]; then
        shred -v ~/.gnupg/*
        rm -rf ~/.gnupg
    fi

    # generation of gpg dynamical
    # source ../.travis/gpg.sh

    mvn --settings ../.travis/settings.xml clean deploy -DskipTests=true -B -U -Prelease

    if [ ! -z "$TRAVIS" ]; then
        shred -v ~/.gnupg/*
        rm -rf ~/.gnupg
    fi
else
    echo "not on a tag -> keep snapshot version in pom.xml"
fi