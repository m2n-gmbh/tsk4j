#!/bin/sh

cp core/src/main/native/Linux/x86_64/Makefile.env.sample core/src/main/native/Linux/x86_64/Makefile.env

echo "
set -Eeuo pipefail

microdnf install -y unzip gzip vim dnf && microdnf clean all
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
dnf install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-9.noarch.rpm
dnf install -y gcc g++ git autoconf gettext libtool wget maven patch

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
./configure --enable-static --disable-shared --with-pic --with-zlib=/usr/local
make && make install

####################
# Install SleuthKit
####################
cd /usr/src
wget https://github.com/sleuthkit/sleuthkit/releases/download/sleuthkit-4.15.0/sleuthkit-4.15.0.tar.gz
tar -xvzf sleuthkit-4.15.0.tar.gz
cd sleuthkit-4.15.0
echo \"
--- tsk/fs/apfs_compat.cpp~     2026-05-07 17:12:01.000000000 +0200
+++ tsk/fs/apfs_compat.cpp      2026-05-07 17:12:18.208476562 +0200
@@ -648,7 +648,7 @@

   fs_file->meta->reset_content = [](void* content_ptr) {
     // Destruct the APFSJObject
-    static_cast<APFSJObject*>(content_ptr)->~APFSJObject();
+    //static_cast<APFSJObject*>(content_ptr)->~APFSJObject();
   };

   auto inode_ptr = static_cast<APFSJObject*>(fs_file->meta->content_ptr);\" | patch -p0
./configure --enable-static --disable-shared --with-pic --disable-java \
    --with-libewf=/usr/local --with-zlib=/usr/local
make && make install

##############
# Build TSK4J
##############
cd /usr/src/tsk4j
mvn clean install -Pnative

" | docker run --rm -i --user root -v $(pwd):/usr/src/tsk4j --entrypoint bash registry.access.redhat.com/ubi9/openjdk-21:latest
