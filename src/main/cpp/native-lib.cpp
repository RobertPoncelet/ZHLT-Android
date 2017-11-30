#include <jni.h>
#include <string>
#include "hlcsg/hlcsg.h"
#include "common/log.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_test_zhlt_1android_MainActivity_hlcsgMain(
        JNIEnv *env,
        jobject /* this */,
        jstring filePath) {
    std::string hello = "Hello from C++";

    start_logger("ZHLT");

    hlcsg_args_t args;
    args.wadConfigName = "";
    args.brushUnionThreshold = 0.0f;
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
    const char* cpath = (*env).GetStringUTFChars(filePath, 0);
    args.mapName = std::string(cpath);
    (*env).ReleaseStringUTFChars(filePath, cpath);

    try {
        hlcsg_main(args);
    }
    catch (const char* msg) {
        return env->NewStringUTF(msg);
    }

    return env->NewStringUTF(hello.c_str());
}
