arch=${1}
x264arch=${2}
mkdir ./../../obj/local/$arch/
cp ./ffmpeg/android/$arch/lib/*.so ./../../obj/local/$arch/
cp ./x264/android/$x264arch/lib/*.so ./../../obj/local/$arch/