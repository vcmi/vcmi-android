@echo off
set BOOST_FOLDER=boost_1_61_0
set NDK_GCC_VER=4.9
set NDK_BUILD_PLATFORM=android-19
set NDK_ROOT=Q:\P\Android\android-ndk-r13b
set NDK_PREBUILT_ARCH=windows-x86_64
set path=%path%;%NDK_ROOT%\toolchains\x86-%NDK_GCC_VER%\prebuilt\%NDK_PREBUILT_ARCH%\bin
set path=%path%;%NDK_ROOT%\toolchains\arm-linux-androideabi-%NDK_GCC_VER%\prebuilt\%NDK_PREBUILT_ARCH%\bin