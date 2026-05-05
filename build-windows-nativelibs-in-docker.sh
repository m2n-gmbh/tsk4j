#!/bin/sh

cp core/src/main/native/Windows/x86_64/Makefile.env.sample core/src/main/native/Windows/x86_64/Makefile.env

echo "
set -Eeuo pipefail

microdnf install -y unzip gzip vim dnf && microdnf clean all
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
cat > /etc/yum.repos.d/centos-stream.repo << 'EOF'
[cs9-baseos]
name=CentOS Stream 9 - BaseOS
baseurl=https://mirror.stream.centos.org/9-stream/BaseOS/x86_64/os/
gpgcheck=1
gpgkey=https://www.centos.org/keys/RPM-GPG-KEY-CentOS-Official
enabled=1

[cs9-appstream]
name=CentOS Stream 9 - AppStream
baseurl=https://mirror.stream.centos.org/9-stream/AppStream/x86_64/os/
gpgcheck=1
gpgkey=https://www.centos.org/keys/RPM-GPG-KEY-CentOS-Official
enabled=1

[cs9-crb]
name=CentOS Stream 9 - CRB
baseurl=https://mirror.stream.centos.org/9-stream/CRB/x86_64/os/
gpgcheck=1
gpgkey=https://www.centos.org/keys/RPM-GPG-KEY-CentOS-Official
enabled=1
EOF

rpm --import https://www.centos.org/keys/RPM-GPG-KEY-CentOS-Official
dnf clean all
dnf install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-9.noarch.rpm
dnf install -y mingw64-gcc mingw64-gcc-c++ git autoconf gettext libtool wget maven

export CHOST=\"x86_64-w64-mingw32\"
export CFLAGS=\"-D_GNU_SOURCE -D_FILE_OFFSET_BITS=64\"
export CXXFLAGS=\"-D_GNU_SOURCE -D_FILE_OFFSET_BITS=64\"

###############
# Install zlib
###############
cd /usr/src
wget https://github.com/madler/zlib/releases/download/v1.3.2/zlib-1.3.2.tar.gz
tar -xvzf zlib-1.3.2.tar.gz
cd zlib-1.3.2
./configure --host=x86_64-w64-mingw32 --prefix=/usr/x86_64-w64-mingw32 --static
make && make install

#################
# Install libewf
#################
cd /usr/src
wget https://github.com/libyal/libewf-legacy/releases/download/20140816/libewf-20140816.tar.gz
tar -xvzf libewf-20140816.tar.gz
cd libewf-20140816
./configure --host=x86_64-w64-mingw32 --prefix=/usr/x86_64-w64-mingw32 --enable-static --disable-shared --with-pic \
    --with-zlib=/usr/x86_64-w64-mingw32
make && make install

####################
# Install SleuthKit
####################
cd /usr/src
wget https://github.com/sleuthkit/sleuthkit/releases/download/sleuthkit-4.15.0/sleuthkit-4.15.0.tar.gz
tar -xvzf sleuthkit-4.15.0.tar.gz
cd sleuthkit-4.15.0
./configure --host=x86_64-w64-mingw32 --prefix=/usr/x86_64-w64-mingw32 --with-pic --enable-static --disable-shared --with-pic \
    --disable-java --with-libewf=/usr/x86_64-w64-mingw32 --with-zlib=/usr/x86_64-w64-mingw32 \
    CC=x86_64-w64-mingw32-gcc CXX=x86_64-w64-mingw32-g++
make && make install

##############
# Build TSK4J
##############
mkdir -p /usr/lib/jvm/java-21-openjdk/include/win32
cat > /usr/lib/jvm/java-21-openjdk/include/win32/jni_md.h << 'EOF'
#ifndef _JAVASOFT_JNI_MD_H_
#define _JAVASOFT_JNI_MD_H_
#define JNIEXPORT __declspec(dllexport)
#define JNIIMPORT __declspec(dllimport)
#define JNICALL   __stdcall
typedef long jint;
typedef long long jlong;
typedef signed char jbyte;
#endif
EOF

cd /usr/src/tsk4j
mvn clean install -Pnative -Pwindows -P\!linux

" | docker run --rm -i --user root -v $(pwd):/usr/src/tsk4j --entrypoint bash registry.access.redhat.com/ubi9/openjdk-21:latest
