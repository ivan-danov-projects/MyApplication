#!/bin/bash

OWNER=IvanDanov
COUNTRY=BG
APP=MyApplication
KEY_PASS=password
STORE_PASS=password

keytool -genkeypair \
	-dname "cn=${APP}, ou=Security, o=${OWNER}, c=${COUNTRY}" \
	-alias "${APP}Key" \
	-keyalg  RSA  -keypass ${KEY_PASS} \
	-keystore ${OWNER}.keystore -storepass ${STORE_PASS} \
	-validity 11000

keytool -list -keystore ${OWNER}.keystore -storepass ${STORE_PASS}

base64 ${OWNER}.keystore > ${OWNER}.keystore.base64

echo "Set GITHUB SECRETS"
echo "JKS: " $(cat ${OWNER}.keystore.base64)
echo "JKS_PWD: ${STORE_PASS}"
echo "JKS_KEY_ALIAS: ${APP}Key"
echo "JKS_KEY_PWD: ${KEY_PASS}"
