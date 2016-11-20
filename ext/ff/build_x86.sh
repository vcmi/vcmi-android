#!/bin/bash
FFDIR=`dirname $0`
NDK=$1
PREBUILT=$NDK/toolchains/x86-4.9/prebuilt/linux-x86_64
PLATFORM=$NDK/platforms/android-18/arch-x86/
GENERAL="\
--enable-small \
--enable-cross-compile \
--extra-libs="-lgcc" \
--cc=$PREBUILT/bin/i686-linux-android-gcc \
--cross-prefix=$PREBUILT/bin/i686-linux-android- \
--nm=$PREBUILT/bin/i686-linux-android-nm \
--extra-cflags="-I../x264/android/i686/include" \
--extra-ldflags="-L../x264/android/i686/lib""
MODULES="\
--enable-gpl \
--enable-libx264"

cd $FFDIR/ffmpeg
./configure \
--logfile=conflog.txt \
--target-os=linux \
--prefix=./android/x86 \
--arch=x86 \
${GENERAL} \
--sysroot=$PLATFORM \
--extra-cflags=" -O3 -DANDROID -Dipv6mr_interface=ipv6mr_ifindex -fasm -Wno-psabi -fno-short-enums -fno-strict-aliasing -fomit-frame-pointer -march=k8" \
--enable-shared \
--disable-static \
--extra-cflags="-march=i686 -mtune=intel -mssse3 -mfpmath=sse -m32" \
--extra-ldflags="-lx264 -Wl,-rpath-link=$PLATFORM/usr/lib -L$PLATFORM/usr/lib -nostdlib -lc -lm -ldl -llog" \
--enable-zlib \
--disable-doc \
${MODULES}

make
make install