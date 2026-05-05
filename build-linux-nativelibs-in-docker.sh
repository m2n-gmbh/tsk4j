#!/bin/sh

echo "
set -Eeuo pipefail

microdnf install -y unzip gzip vim dnf && microdnf clean all
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
dnf install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-9.noarch.rpm
dnf install -y gcc g++ git autoconf gettext libtool wget maven

###############
# Install zlib
###############
cd /usr/src
wget https://github.com/madler/zlib/releases/download/v1.3.2/zlib-1.3.2.tar.gz
tar -xvzf zlib-1.3.2.tar.gz
cd zlib-1.3.2
./configure --static
make && make install

#################
# Install libewf
#################
cd /usr/src
wget https://github.com/libyal/libewf-legacy/releases/download/20140816/libewf-20140816.tar.gz
tar -xvzf libewf-20140816.tar.gz
cd libewf-20140816
./configure --with-pic --enable-static
make && make install

####################
# Install SleuthKit
####################
cd /usr/src
wget https://github.com/sleuthkit/sleuthkit/releases/download/sleuthkit-4.15.0/sleuthkit-4.15.0.tar.gz
tar -xvzf sleuthkit-4.15.0.tar.gz
cd sleuthkit-4.15.0
./configure --enable-static --with-libewf=/usr/local --with-zlib=/usr/local
make && make install

##############
# Build TSK4J
##############
cd /usr/src/tsk4j
cp core/src/main/native/Linux/x86_64/Makefile.env.sample core/src/main/native/Linux/x86_64/Makefile.env
mvn clean install -Pnative

" | docker run --rm -i --user root -v $(pwd):/usr/src/tsk4j --entrypoint bash registry.access.redhat.com/ubi9/openjdk-21:latest
