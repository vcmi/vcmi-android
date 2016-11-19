#!/bin/bash
#Change NDK to your Android NDK location
NDK=/mnt/q/P/Android/android-ndk-r13b-linux
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
build_ARMv6()
{
cd ./ffmpeg
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
make clean
make
make install
}
build_ARMv6
echo Android ARMEABI builds finished