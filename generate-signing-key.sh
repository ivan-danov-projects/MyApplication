#!/bin/bash

OWNER="Ivan Danov"
COUNTRY=BG
APP="MyApplication"
KEY_PASS=password
STORE_PASS=password

keytool -genkeypair \
	-dname "cn=${APP}, ou=Security, o=${OWNER}, c=${COUNTRY}" \
	-alias "${APP} Key" \
	-keyalg  RSA  -keypass ${KEY_PASS} \
	-keystore "${APP}.keystore" -storepass ${STORE_PASS} \
	-validity 11000

keytool -list -keystore "${APP}.keystore" -storepass ${STORE_PASS}

base64 --wrap=0 "${APP}.keystore" > "${APP}.keystore.base64"

# generate local .signing file
cat > ${APP}.signing << EOF
keystore=../${APP}.keystore
keystore.password=${STORE_PASS}
keyAlias=${APP} Key
keyPassword=${KEY_PASS}
EOF

echo "Set GITHUB SECRETS"
echo "JKS: " $(cat "${APP}.keystore.base64")
echo "JKS_PWD: ${STORE_PASS}"
echo "JKS_KEY_ALIAS: ${APP}Key"
echo "JKS_KEY_PWD: ${KEY_PASS}"

# generate debug keystore
keytool -genkey -v -keystore "${APP}.debug.keystore" -storepass android \
	-alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 \
	-validity 10000 -dname "C=US, O=Android, CN=Android Debug"
base64 --wrap=0 "${APP}.debug.keystore" > "${APP}.debug.keystore.base64"
echo "DEBUGJKS: " $(cat "${APP}.debug.keystore.base64")

# https://gist.github.com/henriquemenezes/70feb8fff20a19a65346e48786bedb8f
