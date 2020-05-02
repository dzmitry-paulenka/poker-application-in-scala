#!/bin/bash

cat ~/.ssh/dokku-host.pub | ssh dokku-host "sudo sshcommand acl-add dokku root"

git remote add poker-frontend dokku@dokku-host:poker.paulenka.xyz
git remote add poker-api dokku@dokku-host:poker-api.paulenka.xyz
