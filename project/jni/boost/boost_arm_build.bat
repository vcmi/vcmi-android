rem @echo off
cd %BOOST_FOLDER%

rmdir bin.v2\libs /s /q
rmdir stage\lib /s /q

b2 install --without-python --without-serialization ^
	threading=multi link=static runtime-link=static toolset=gcc-android target-os=android threadapi=pthread --stagedir=android stage


rem b2 link=static threading=multi threadapi=pthread target-os=android ^
rem 	toolset=gcc-arm ^
rem 	--without-context --without-coroutine --without-log ^
rem 	--without-test --without-atomic ^
rem 	--without-graph --without-graph_parallel --without-math --without-python ^
rem 	--without-signals --without-wave --without-timer --without-mpi ^
rem 	define=BOOST_MATH_DISABLE_FLOAT128 ^
rem 	include=%NDK_ROOT%\sources\cxx-stl\gnu-libstdc++\%NDK_GCC_VER%\include ^
rem 	include=%NDK_ROOT%\sources\cxx-stl\gnu-libstdc++\%NDK_GCC_VER%\libs\armeabi\include ^
rem 	include=%NDK_ROOT%\platforms\%NDK_BUILD_PLATFORM%\arch-arm\usr\include

set ARPATH=%NDK_ROOT%\toolchains\arm-linux-androideabi-%NDK_GCC_VER%\prebuilt\%NDK_PREBUILT_ARCH%\bin\arm-linux-androideabi-ar.exe
set OUT_TMP_DIR=out_tmp_arm
set OUTLIB=libboost_armeabi.a

rmdir %OUT_TMP_DIR% /s /q
mkdir %OUT_TMP_DIR%
cd %OUT_TMP_DIR%

FOR %%f in (%OUT_TMP_DIR%\*.o) DO %ARPATH% qv %OUTLIB% %%f

cd ..

%ARPATH% qv %OUTLIB% %OUT_TMP_DIR%\*.o

cd ..