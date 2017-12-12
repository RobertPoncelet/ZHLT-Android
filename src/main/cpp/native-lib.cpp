#include <jni.h>
#include <string>
#include "hlcsg/hlcsg.h"
#include "common/log.h"

extern int             hlbsp_main(const char* map);

extern "C"
JNIEXPORT jstring JNICALL
Java_test_zhlt_1android_MainActivity_hlcsgMain(
        JNIEnv *env,
        jobject /* this */,
        jstring filePath) {
    std::string hello = "Hello from C++";

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
