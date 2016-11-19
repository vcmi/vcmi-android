#!/bin/bash
#Change NDK to your Android NDK location
NDK=/mnt/q/P/Android/android-ndk-r13b-linux
PREBUILT=$PWD/ffmpeg-toolchain
PLATFORM=$PREBUILT/sysroot
GENERAL="\
--disable-doc   --disable-ffplay   --disable-ffmpeg   --disable-ffprobe   --disable-ffserver   --disable-avdevice   --disable-avfilter   --disable-encoders    --disable-muxers   --disable-filters   --disable-devices   --disable-everything
--enable-small \
--enable-cross-compile \
--extra-libs="-lgcc" \
--cc=$PREBUILT/bin/i686-linux-android-gcc \
--cross-prefix=$PREBUILT/bin/i686-linux-android- \
--nm=$PREBUILT/bin/i686-linux-android-nm"
MODULES="\
--enable-gpl"
build_ARMv6()
{
cd ./ffmpeg
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
--extra-ldflags="-Wl,-rpath-link=$PLATFORM/usr/lib -L$PLATFORM/usr/lib -nostdlib -lc -lm -ldl -llog" \
--enable-zlib \
--disable-doc \
${MODULES}
#make clean
#make
#make install
}
build_ARMv6
echo Android ARMEABI builds finished