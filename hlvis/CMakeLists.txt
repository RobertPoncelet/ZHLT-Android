cmake_minimum_required(VERSION 2.8)
project(hlvis)

set(CMAKE_BUILD_TYPE Release)

include_directories(../common)
include_directories(../template)
include_directories(.)

#g++ autowad.cpp brush.cpp brushunion.cpp hullfile.cpp map.cpp properties.cpp qcsg.cpp textures.cpp wadcfg.cpp wadinclude.cpp wadpath.cpp ../common/bspfile.cpp ../common/filelib.cpp ../common/blockmem.cpp 
#../common/files.cpp ../common/log.cpp ../common/mathlib.cpp ../common/messages.cpp ../common/scriplib.cpp ../common/threads.cpp ../common/winding.cpp ../common/cmdlib.cpp -g -I. -I../common -I../template 
#-D DOUBLEVEC_T -D THREAD_PRIORITY_ABOVE_NORMAL=1 -D CDECL= -D _MAX_PATH=4096 -D STDC_HEADERS -D UINT32=unsigned -D SYSTEM_POSIX -D HAVE_UNISTD_E -D HAVE_UNISTD_H -D HAVE_FCNTL_H -D HAVE_PTHREAD_H -D HAVE_SYS_STAT_H 
#-D HAVE_SYS_TIME_H -D HAVE_SYS_ATOMIC_H -D HAVE_ATOMIC -D HAVE_STDDEF_H -D HAVE_SYS_TIME_H -D HAVE_SYS_RESOURCE_H -D HLCSG_NULLIFY_INVISIBLE -lm -lpthread -fpermissive
add_definitions(
	-D__STDC_WANT_LIB_EXT1__
	-DDOUBLEVEC_T
	-DHLRAD_FASTMATH
	-DCDECL= 
	-DSTDC_HEADERS 
	-DUINT32=uint32
	-DSYSTEM_POSIX
	-DSINGLE_THREADED
	
	-DHAVE_UNISTD_E 
	-DHAVE_UNISTD_H
	-DHAVE_FCNTL_H
	-DHAVE_PTHREAD_H
	-DHAVE_SYS_STAT_H 
	-DHAVE_SYS_TIME_H 
	-DHAVE_SYS_ATOMIC_H
	-DHAVE_ATOMIC
	-DHAVE_STDDEF_H
	-DHAVE_SYS_TIME_H
	-DHAVE_SYS_RESOURCE_H
	
	-Dsprintf_s=sprintf
	-Dstrcpy_s=strcpy
	-Dsscanf_s=sscanf
	-Dfscanf_s=fscanf
	-D_strupr=strupr
	-D_strdup=strdup
	-D_strlwr=strlwr
	-D_unlink=unlink
	-D_open=open
	-D_close=close
	-D_read=read
)

#remove_definitions(-DHLRAD_FASTMATH -DDOUBLEVEC_T)

#set(CMAKE_C_FLAGS "-lm -lpthread -fpermissive")
set(CMAKE_CXX_FLAGS "-g -lm -lpthread -fpermissive")

#Use all cpps in the current directory, and add some in common
file(GLOB PROJECT_SOURCES "*.cpp")
file(GLOB COMMON_SOURCES "../common/*.cpp")
#set(COMMON_SOURCES 
#	../common/bspfile.cpp 
#	../common/filelib.cpp 
#	../common/blockmem.cpp ls 
#	../common/files.cpp 
#	../common/log.cpp 
#	../common/mathlib.cpp 
#	../common/messages.cpp 
#	../common/scriplib.cpp 
#	../common/threads.cpp 
#	../common/winding.cpp 
#	../common/cmdlib.cpp
#)
set(SOURCES ${PROJECT_SOURCES} ${COMMON_SOURCES})

add_executable(hlvis ${SOURCES})
