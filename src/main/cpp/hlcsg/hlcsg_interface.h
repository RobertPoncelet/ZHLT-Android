//
// Created by r.poncelet on 13/12/17.
//

#ifndef ZHLT_ANDROID_HLCSG_INTERFACE_H
#define ZHLT_ANDROID_HLCSG_INTERFACE_H

#include <string>
#include <deque>

typedef struct
{
    std::string     mapName;
    int             numThreads;
    bool            estimate;
    int             developerLevel;
    bool            verbose;
    bool            info;
    bool            chart;
    int             threadPriority;
    bool            log;
    bool            skyClip;
    bool            noClip;
    bool            onlyEnts;
    bool            useNullTex;
    bool            clipNazi;
    int             clipType;
    std::string     wadConfigName;
    std::string     wadCfgFile;
    std::string     nullFile;
    bool            wadAutoDetect;
    bool            wadTextures;
    std::deque< std::string >   wadInclude;
    int             texData;
    int             lightData;
    float           brushUnionThreshold;
    float           tinyThreshold;
    std::string     hullfile;
} hlcsg_args_t;

int hlcsg_main(hlcsg_args_t);

#endif //ZHLT_ANDROID_HLCSG_INTERFACE_H
