#!/bin/bash
FFDIR=`dirname $0`
NDK=$1
OUTPUT_DIR=$2

cleanup()
{
	cd $FFDIR/ffmpeg
	make clean
	rm -f $FFDIR/ffmpeg/compat/strtod.o #clean leaves this file behind, breaking the rebuilds; lets remove it manually
	rm -f $FFDIR/ffmpeg/compat/strtod.d
}

assert_zero()
{
	if [ $1 -eq 0 ]; then
		echo $2
	else
		echo $3
		exit 1
	fi
}

build_arch()
{
	cleanup
	sh $FFDIR/build_${1}.sh $NDK
	assert_zero $? "Built FFMPEG-$1" "Could not build FFMPEG-$1"
}

copy_output()
{
	ARCH=$1
	ARCHX264=$2
	mkdir -p $OUTPUT_DIR/$ARCH/
	echo copying from $FFDIR/ffmpeg/android/$ARCH/lib
	echo copying to $OUTPUT_DIR/$ARCH/
	
	find $FFDIR/ffmpeg/android/$ARCH/lib | grep ".*/lib[^\-]*\.so$" | xargs -i cp {} $OUTPUT_DIR/$ARCH
	assert_zero $? "Copied FFMPEG libs for $ARCH" "Could not copy FFMPEG libs for $ARCH"
	find $FFDIR/x264/android/$ARCHX264/lib | grep ".*/lib[^\-]*\.so$" | xargs -i cp {} $OUTPUT_DIR/$ARCH
	assert_zero $? "Copied x264 libs for $ARCHX264" "Could not copy x264 libs for $ARCHX264"
}

sh $FFDIR/bx264.sh $NDK
assert_zero $? "Built x264" "Could not build x264"

build_arch arm
build_arch arm_v7a
build_arch arm64_v8a
build_arch x86
build_arch x86_64

copy_output armeabi arm
copy_output armeabi-v7a arm
copy_output arm64-v8a aarch64
copy_output x86 i686
copy_output x86_64 x86_64

mkdir -p $FFDIR/include
cp -r $FFDIR/ffmpeg/android/armeabi/include/* $FFDIR/include/
assert_zero $? "Copied FFMPEG includes" "Could not copy FFMPEG includes"
cp -r $FFDIR/x264/android/arm/include/* $FFDIR/include/
assert_zero $? "Copied x264 includes" "Could not copy x264 includes"

