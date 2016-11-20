#!/bin/bash
FFDIR=`dirname $0`
NDK=$1
PREBUILT=$NDK/toolchains/x86_64-4.9/prebuilt/linux-x86_64
PLATFORM=$NDK/platforms/android-21/arch-x86_64/
GENERAL="\
--enable-small \
--enable-cross-compile \
--extra-libs="-lgcc" \
--cc=$PREBUILT/bin/x86_64-linux-android-gcc \
--cross-prefix=$PREBUILT/bin/x86_64-linux-android- \
--nm=$PREBUILT/bin/x86_64-linux-android-nm \
--extra-cflags="-I../x264/android/x86_64/include" \
--extra-ldflags="-L../x264/android/x86_64/lib" "
MODULES="\
--enable-gpl \
--enable-libx264"

cd $FFDIR/ffmpeg
./configure \
--logfile=conflog.txt \
--target-os=linux \
--prefix=./android/x86_64 \
--arch=x86_64 \
${GENERAL} \
--sysroot=$PLATFORM \
--extra-cflags="-march=x86-64 -msse4.2 -mpopcnt -m64 -mtune=intel -fasm" \
--enable-shared \
--disable-static \
--extra-ldflags="-lx264 -Wl,-rpath-link=$PLATFORM/usr/lib64 -L$PLATFORM/usr/lib64 -nostdlib -lc -lm -ldl -llog" \
--enable-zlib \
--disable-doc \
${MODULES}

make
make install