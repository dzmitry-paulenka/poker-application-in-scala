#!/bin/bash

dokku plugin:install https://github.com/dokku/dokku-letsencrypt.git
dokku plugin:install https://gitlab.com/notpushkin/dokku-monorepo
dokku plugin:install https://github.com/dokku/dokku-mongo.git mongo

dokku config:set --global DOKKU_LETSENCRYPT_EMAIL=admin@paulenka.xyz

dokku apps:create poker.paulenka.xyz
dokku apps:create poker-api.paulenka.xyz

dokku mongo:create poker
dokku mongo:link poker poker-api.paulenka.xyz

dokku letsencrypt poker.paulenka.xyz
dokku letsencrypt poker-api.paulenka.xyz
