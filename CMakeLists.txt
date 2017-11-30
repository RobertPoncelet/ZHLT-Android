# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

add_library( native-lib SHARED src/main/cpp/native-lib.cpp )

find_library( log-lib log )

target_link_libraries( native-lib ${log-lib} )

#set(CMAKE_CXX_FLAGS "-g -lm -lpthread -fpermissive")
#set(CMAKE_CXX_FLAGS "--fpermissive")

include_directories(src/main/cpp/common)
include_directories(src/main/cpp/template)

set(COMMON_DEFINITIONS
	__STDC_WANT_LIB_EXT1__
	CDECL=
	STDC_HEADERS
	UINT32=uint32
	SYSTEM_POSIX
	SINGLE_THREADED
	SIZEOF_CHARP=8

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

file(GLOB COMMON_SOURCES "src/main/cpp/common/*.cpp")

foreach (proj ${PROJECTS})
	include_directories(${proj})
	file(GLOB PROJECT_SOURCES "src/main/cpp/${proj}/*.cpp")

	list(REMOVE_ITEM PROJECT_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/${proj}/${proj}.cpp")
	MESSAGE(STATUS "Excluding executable source ${CMAKE_CURRENT_SOURCE_DIR}/${proj}/${proj}.cpp")

	set(SOURCES ${PROJECT_SOURCES} ${COMMON_SOURCES})

	add_library(${proj} SHARED ${SOURCES})

endforeach (proj ${PROJECTS})

separate_arguments(FINAL_COMMON_DEFINITIONS UNIX_COMMAND "${COMMON_DEFINITIONS}")
set_target_properties(hlbsp PROPERTIES COMPILE_DEFINITIONS ${FINAL_COMMON_DEFINITIONS}\;DOUBLEVEC_T)
set_target_properties(hlcsg PROPERTIES COMPILE_DEFINITIONS ${FINAL_COMMON_DEFINITIONS}\;DOUBLEVEC_T)
set_target_properties(hlrad PROPERTIES COMPILE_DEFINITIONS ${FINAL_COMMON_DEFINITIONS})
set_target_properties(hlvis PROPERTIES COMPILE_DEFINITIONS ${FINAL_COMMON_DEFINITIONS}\;DOUBLEVEC_T)

set_target_properties(native-lib PROPERTIES COMPILE_DEFINITIONS ${FINAL_COMMON_DEFINITIONS}\;DOUBLEVEC_T)
target_link_libraries(native-lib hlcsg)

target_link_libraries(hlcsg ${log-lib})
target_link_libraries(hlbsp ${log-lib})
target_link_libraries(hlrad ${log-lib})
target_link_libraries(hlvis ${log-lib})