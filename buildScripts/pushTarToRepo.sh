#!/bin/bash

mkdir pushHadoopTar/HadoopSourceBinary
cp -rf hadoop-dist/target/hadoop-2.9.0 pushHadoopTar/HadoopSourceBinary/
ls

cd pushHadoopTar
ls
mkdir -p HadoopSourceBinary
mkdir -p deb/tmp/
cp -rf HadoopSourceBinary deb/tmp/
cp -rf DEBIAN deb/

dpkg -b deb flipkart-hadoop-2.9.0.tar-test.deb
reposervice --host repo-svc-app-0001.nm.flipkart.com --port "8080" pubrepo --repo  fk-hadoop-2.9.0.tar-test-source  --appkey dummy --debs flipkart-hadoop-2.9.0.tar-test.deb
cd $cwd
