#!/bin/bash
#Change NDK to your Android NDK location
NDK=/mnt/q/P/Android/android-ndk-r13b-linux
PREBUILT=$NDK/toolchains/x86_64-4.9/prebuilt/linux-x86_64
PLATFORM=$NDK/platforms/android-16/arch-x86_64/
GENERAL="\
--disable-doc   --disable-ffplay   --disable-ffmpeg   --disable-ffprobe   --disable-ffserver   --disable-avdevice   --disable-avfilter   --disable-encoders    --disable-muxers   --disable-filters   --disable-devices   --disable-everything
--enable-small \
--enable-cross-compile \
--extra-libs="-lgcc" \
--cc=$PREBUILT/bin/x86_64-linux-android-gcc \
--cross-prefix=$PREBUILT/bin/x86_64-linux-android- \
--nm=$PREBUILT/bin/x86_64-linux-android-nm"
MODULES="\
--enable-gpl"
build_ARMv6()
{
cd ./ffmpeg
 ./configure \
 --logfile=conflog.txt \
--target-os=linux \
--prefix=./android/x86_64 \
--arch=x86_64 \
${GENERAL} \
--sysroot=$PLATFORM \
--extra-cflags="-march=x86-64 -msse4.2 -mpopcnt -m64 -mtune=intel" \
--enable-shared \
--disable-static \
--extra-ldflags="-Wl,-rpath-link=$PLATFORM/usr/lib64 -L$PLATFORM/usr/lib64 -nostdlib -lc -lm -ldl -llog" \
--enable-zlib \
--disable-doc \
${MODULES}
make clean
make
make install

}
build_ARMv6
echo Android ARMEABI builds finished