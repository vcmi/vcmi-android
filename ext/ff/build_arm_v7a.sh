#!/bin/bash
FFDIR=`dirname $0`
NDK=$1
PREBUILT=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
PLATFORM=$NDK/platforms/android-16/arch-arm/
GENERAL="\
--enable-small \
--enable-cross-compile \
--extra-libs="-lgcc" \
--arch=arm \
--cc=$PREBUILT/bin/arm-linux-androideabi-gcc \
--cross-prefix=$PREBUILT/bin/arm-linux-androideabi- \
--nm=$PREBUILT/bin/arm-linux-androideabi-nm \
--extra-cflags="-I../x264/android/arm/include" \
--extra-ldflags="-L../x264/android/arm/lib" "
MODULES="\
--enable-gpl \
--enable-libx264"

cd $FFDIR/ffmpeg
./configure \
--target-os=linux \
--prefix=./android/armeabi-v7a \
${GENERAL} \
--sysroot=$PLATFORM \
--enable-shared \
--disable-static \
--extra-cflags="-DANDROID -fPIC -ffunction-sections -funwind-tables -fstack-protector -march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -fomit-frame-pointer -fstrict-aliasing -funswitch-loops -finline-limit=300" \
--extra-ldflags="-Wl,-rpath-link=$PLATFORM/usr/lib -L$PLATFORM/usr/lib -nostdlib -lc -lm -ldl -llog" \
--enable-zlib \
${MODULES} \
--disable-doc \
--enable-neon

make
make install