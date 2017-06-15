#!/bin/sh
touch /tmp/deploy_rsa
chmod 600 /tmp/deploy_rsa
openssl aes-256-cbc -K $encrypted_a9294e222946_key -iv $encrypted_a9294e222946_iv -in $TRAVIS_BUILD_DIR/CI/deploy_rsa_android.enc -out /tmp/deploy_rsa -d
eval "$(ssh-agent -s)"
ssh-add /tmp/deploy_rsa

PACKAGE_FULL_PATH=$(find $TRAVIS_BUILD_DIR -type f -name "${TRAVIS_JOB_ID}-vcmi-*.apk")
VCMI_PACKAGE_FILE_NAME=${PACKAGE_FULL_PATH##*/}

sftp -r -o StrictHostKeyChecking=no travis-android@beholder.vcmi.eu <<< "put $PACKAGE_FULL_PATH /incoming/$VCMI_PACKAGE_FILE_NAME"
