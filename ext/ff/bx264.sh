#!/bin/bash
FFDIR=`dirname $0`
NDK=$1

build_arch()
{
ARCH=$1
PLATFORM=$2
TOOLCHAIN=$3
EABISUFFIX=$4
DISABLEASM=$5
cd $FFDIR/x264
./configure \
--prefix=./android/$ARCH \
--enable-shared \
--enable-pic \
$DISABLEASM \
--host=$ARCH-linux \
--cross-prefix=$TOOLCHAIN/bin/$ARCH-linux-android$EABISUFFIX- \
--sysroot=$PLATFORM
make clean
make
make install
}

build_arch arm     $NDK/platforms/android-16/arch-arm/     $NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64 eabi
build_arch aarch64 $NDK/platforms/android-21/arch-arm64/   $NDK/toolchains/aarch64-linux-android-4.9/prebuilt/linux-x86_64
build_arch i686    $NDK/platforms/android-18/arch-x86/     $NDK/toolchains/x86-4.9/prebuilt/linux-x86_64 "" --disable-asm
build_arch x86_64  $NDK/platforms/android-21/arch-x86_64/  $NDK/toolchains/x86_64-4.9/prebuilt/linux-x86_64 ""  --disable-asm