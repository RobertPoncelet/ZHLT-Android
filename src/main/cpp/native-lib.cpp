#include <jni.h>
#include <string>
#include "win32fix.h"
#include "common/log.h"
#include "hlcsg/hlcsg_interface.h"
#include "hlbsp/hlbsp_interface.h"

extern "C"
JNIEXPORT jstring JNICALL
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

    int code = hlcsg_main(args);
    if (code != 0)
    {
        return env->NewStringUTF("Fatal exception in HLCSG!");
    }

    code = hlbsp_main(args.mapName.c_str());
    if (code != 0)
    {
        return env->NewStringUTF("Fatal exception in HLBSP!");
    }

    return env->NewStringUTF(hello.c_str());
}
