cmake_minimum_required(VERSION 2.8)
project(zhlt)

set(CMAKE_BUILD_TYPE Release)

set(CMAKE_CXX_FLAGS "-g -lm -lpthread -fpermissive")

include_directories(common)
include_directories(template)

#g++ autowad.cpp brush.cpp brushunion.cpp hullfile.cpp map.cpp properties.cpp qcsg.cpp textures.cpp wadcfg.cpp wadinclude.cpp wadpath.cpp ../common/bspfile.cpp ../common/filelib.cpp ../common/blockmem.cpp 
#../common/files.cpp ../common/log.cpp ../common/mathlib.cpp ../common/messages.cpp ../common/scriplib.cpp ../common/threads.cpp ../common/winding.cpp ../common/cmdlib.cpp -g -I. -I../common -I../template 
#-D DOUBLEVEC_T -D THREAD_PRIORITY_ABOVE_NORMAL=1 -D CDECL= -D _MAX_PATH=4096 -D STDC_HEADERS -D UINT32=unsigned -D SYSTEM_POSIX -D HAVE_UNISTD_E -D HAVE_UNISTD_H -D HAVE_FCNTL_H -D HAVE_PTHREAD_H -D HAVE_SYS_STAT_H 
#-D HAVE_SYS_TIME_H -D HAVE_SYS_ATOMIC_H -D HAVE_ATOMIC -D HAVE_STDDEF_H -D HAVE_SYS_TIME_H -D HAVE_SYS_RESOURCE_H -D HLCSG_NULLIFY_INVISIBLE -lm -lpthread -fpermissive
#add_definitions(
set(COMMON_DEFINITIONS
	__STDC_WANT_LIB_EXT1__
	CDECL= 
	STDC_HEADERS 
	UINT32=uint32
	SYSTEM_POSIX
	SINGLE_THREADED
	
	HAVE_UNISTD_E 
	HAVE_UNISTD_H
	HAVE_FCNTL_H
	HAVE_PTHREAD_H
	HAVE_SYS_STAT_H 
	HAVE_SYS_TIME_H 
	HAVE_SYS_ATOMIC_H
	HAVE_ATOMIC
	HAVE_STDDEF_H
	HAVE_SYS_TIME_H
	HAVE_SYS_RESOURCE_H
	
	sprintf_s=sprintf
	strcpy_s=strcpy
	sscanf_s=sscanf
	fscanf_s=fscanf
	_strupr=strupr
	_strdup=strdup
	_strlwr=strlwr
	_unlink=unlink
	_open=open
	_close=close
	_read=read
)

set(PROJECTS hlbsp hlcsg hlrad hlvis)

file(GLOB COMMON_SOURCES "common/*.cpp")

foreach (proj ${PROJECTS})
	include_directories(${proj})
	file(GLOB PROJECT_SOURCES "${proj}/*.cpp")
	
	# Only hlrad doesn't use DOUBLEVEC_T
	#if (${proj} STREQUAL "hlrad")
		#remove_definitions(-DDOUBLEVEC_T)
		#set(COMPILE_DEFINITIONS ${COMMON_DEFINITIONS})
		#MESSAGE(STATUS "removing doublevec define for ${proj}")
	#else (${proj} STREQUAL "hlrad")
		#add_definitions(-DDOUBLEVEC_T)
		#set(COMPILE_DEFINITIONS ${COMMON_DEFINITIONS} -DDOUBLEVEC_T)
		#MESSAGE(STATUS "adding doublevec define for ${proj}")
	#endif (${proj} STREQUAL "hlrad")
	
	set(SOURCES ${PROJECT_SOURCES} ${COMMON_SOURCES})
	
	add_executable(${proj} ${SOURCES})
endforeach (proj ${PROJECTS})

separate_arguments(FINAL_COMMON_DEFINITIONS UNIX_COMMAND "${COMMON_DEFINITIONS}")
set_target_properties(hlbsp PROPERTIES COMPILE_DEFINITIONS ${FINAL_COMMON_DEFINITIONS}\;DOUBLEVEC_T )
set_target_properties(hlcsg PROPERTIES COMPILE_DEFINITIONS ${FINAL_COMMON_DEFINITIONS}\;DOUBLEVEC_T )
set_target_properties(hlrad PROPERTIES COMPILE_DEFINITIONS ${FINAL_COMMON_DEFINITIONS} )
set_target_properties(hlvis PROPERTIES COMPILE_DEFINITIONS ${FINAL_COMMON_DEFINITIONS}\;DOUBLEVEC_T )
