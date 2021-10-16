#!/bin/sh
if [ -z "$DEPLOY_RSA" ];
then
	# Due to security measures travis not expose encryption keys for PR from forks
	echo "Build generation is skipped for forks"
	exit 0
fi

echo "$DEPLOY_RSA" > /tmp/deploy_rsa
chmod 600 /tmp/deploy_rsa

eval "$(ssh-agent -s)"
ssh-add /tmp/deploy_rsa

PACKAGE_FULL_PATH=$(find $GITHUB_WORKSPACE/project/vcmi-app/build/outputs -type f -name "*-vcmi-*.apk")
echo "PACKAGE_FULL_PATH = $PACKAGE_FULL_PATH"
VCMI_PACKAGE_FILE_NAME=${PACKAGE_FULL_PATH##*/}
echo "VCMI_PACKAGE_FILE_NAME = $VCMI_PACKAGE_FILE_NAME"

sftp -r -o StrictHostKeyChecking=no travis-android@beholder.vcmi.eu <<< "put $PACKAGE_FULL_PATH /incoming/$VCMI_PACKAGE_FILE_NAME"

