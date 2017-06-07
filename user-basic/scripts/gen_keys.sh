#!/bin/sh
openssl genrsa -out mykey.pem 2048
openssl pkcs8 -topk8 -inform PEM -outform PEM -in mykey.pem -out private_key.pem -nocrypt
openssl rsa -in mykey.pem -pubout -outform PEM -out public_key.pem
