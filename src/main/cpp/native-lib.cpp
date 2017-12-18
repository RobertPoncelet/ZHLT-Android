#include <jni.h>
#include <string>
#include "win32fix.h"
#include "common/log.h"
#include "hlcsg/hlcsg_interface.h"
#include "hlbsp/hlbsp_interface.h"
#include "hlvis/hlvis_interface.h"
#include "hlrad/hlrad_interface.h"

extern "C"
JNIEXPORT jint JNICALL
Java_test_zhlt_1android_MainActivity_hlcsgMain(
        JNIEnv *env,
        jobject /* this */,
        jstring filePath) {
    std::string hello = "Fix me";

    start_logger("ZHLT-Android");

    hlcsg_args_t args;
    args.wadConfigName = "";
    args.brushUnionThreshold = 0.1f;
    args.chart = false;
    args.clipNazi = false;
    args.clipType = 0;
    args.developerLevel = 0;
    args.estimate = false;
    args.hullfile = "";
    args.info = false;
    args.lightData = 0;
    args.log = true;
    args.noClip = false;
    args.nullFile = "";
    args.numThreads = 1;
    args.onlyEnts = false;
    args.skyClip = false;
    args.threadPriority = 0;
    args.tinyThreshold = 0.1;
    const char* cpath = (*env).GetStringUTFChars(filePath, 0);
    args.mapName = std::string(cpath);
    (*env).ReleaseStringUTFChars(filePath, cpath);

    return (jint)hlcsg_main(args);
}

extern "C"
JNIEXPORT jint JNICALL
Java_test_zhlt_1android_MainActivity_hlbspMain(
        JNIEnv *env,
        jobject /* this */,
        jstring filePath) {

    const char* cpath = (*env).GetStringUTFChars(filePath, 0);
    jint result = (jint)hlbsp_main(cpath);
    (*env).ReleaseStringUTFChars(filePath, cpath);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_test_zhlt_1android_MainActivity_hlvisMain(
        JNIEnv *env,
        jobject /* this */,
        jstring filePath) {

    const char* cpath = (*env).GetStringUTFChars(filePath, 0);
    jint result = (jint)hlvis_main(cpath);
    (*env).ReleaseStringUTFChars(filePath, cpath);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_test_zhlt_1android_MainActivity_hlradMain(
        JNIEnv *env,
        jobject /* this */,
        jstring filePath) {

    const char* cpath = (*env).GetStringUTFChars(filePath, 0);
    jint result = (jint)hlrad_main(cpath);
    (*env).ReleaseStringUTFChars(filePath, cpath);

    return result;
}
