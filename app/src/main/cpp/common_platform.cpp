/*==============================================================================
FMOD Example Framework
Copyright (c), Firelight Technologies Pty, Ltd 2013-2021.
==============================================================================*/
#include "common.h"
#include <cstring>
#include <jni.h>
#include <unistd.h>
#include <string>
#include <vector>

JNIEnv *gJNIEnv;
jobject gMainActivityObject;
bool gSuspendState;
bool gQuitState;
std::string gUIString;
std::vector<char *> gPathList;

// 0 - Rain, 1 - Wind, 2 - Cover
float forestParameters[] = {0., 0., 0.};
float countryParameter;
// 0 - Traffic, 1 - Walla
float cityParameters[] = {0., 0.};

int parameterChangedState;

int eventState;
int lastEventState;

int FMOD_Main(); // Defined in example

void Common_Init(void **extraDriverData){
	gSuspendState = false;
	gQuitState = false;
	countryParameter = 0;
    parameterChangedState = 0;
    eventState = 0;
    lastEventState = -1;
}

void Common_Close(){
    for (std::vector<char *>::iterator item = gPathList.begin(); item != gPathList.end(); ++item)
    {
        free(*item);
    }

    gPathList.clear();
}

void Common_Update(){
    parameterChangedState = 0;
    lastEventState = eventState;
}

void Common_Sleep(unsigned int ms){
    usleep(ms * 1000);
}

void Common_Exit(int returnCode){
    exit(returnCode);
}

void Common_LoadFileMemory(const char *name, void **buff, int *length){
    FILE *file = fopen(name, "rb");
    
    fseek(file, 0, SEEK_END);
    long len = ftell(file);
    fseek(file, 0, SEEK_SET);
    
    void *mem = malloc(len);
    fread(mem, 1, len, file);
    
    fclose(file);

    *buff = mem;
    *length = len;
}

void Common_UnloadFileMemory(void *buff){
    free(buff);
}

bool Common_QuitState(){
    return gQuitState;
}

// 0 - Rain
float Common_ForestGetRainParameter(){
    return forestParameters[0];
}

// 1 - Wind
float Common_ForestGetWindParameter(){
    return forestParameters[1];
}

// 2 - Cover
float Common_ForestGetCoverParameter(){
    return forestParameters[2];
}

float Common_CountryGetParameter(){
    return countryParameter;
}

// 0 - Traffic
float Common_CityGetTrafficParameter(){
    return cityParameters[0];
}

// 1 - Walla
float Common_CityGetWallaParameter(){
    return cityParameters[1];
}

// 0 - Forest, 1 - Country, 2 - City
bool Common_ParameterIsChanged(int event){
    return (parameterChangedState & (1 << event)) != 0;
}

bool Common_EventIsChanged(){
    return eventState != lastEventState;
}

// 0 - Forest, 1 - Country, 2 - City
bool Common_EventState(int event)
{
    return (eventState & (1 << event)) != 0;
}

const char *Common_MediaPath(const char *fileName)
{
    char *filePath = (char *)calloc(256, sizeof(char));

    strcat(filePath, "file:///android_asset/");
    strcat(filePath, fileName);
    gPathList.push_back(filePath);

    return filePath;
}

const char *Common_WritePath(const char *fileName)
{
	return Common_MediaPath(fileName);
}

bool Common_SuspendState()
{
	return gSuspendState;
}

extern "C"
{

void Java_ru_wald_1t_sound_1of_1nature_services_PlayAudioService_forestSetParameter(
        JNIEnv *env,
        jobject thiz,
        jfloat rain,
        jfloat wind,
        jfloat cover)
{
    forestParameters[0] = rain;
    forestParameters[1] = wind;
    forestParameters[2] = cover;
    parameterChangedState |= (1 << 0); // 0 - Forest state
}

void Java_ru_wald_1t_sound_1of_1nature_services_PlayAudioService_countrySetParameter(JNIEnv *env, jobject thiz, jfloat index){
    countryParameter = index;
    parameterChangedState |= (1 << 1); // 1 - Country state
}

void Java_ru_wald_1t_sound_1of_1nature_services_PlayAudioService_citySetParameter(
        JNIEnv *env,
        jobject thiz,
        jfloat traffic,
        jfloat walla)
{
    cityParameters[0] = traffic;
    cityParameters[1] = walla;
    parameterChangedState |= (1 << 2); // 2 - City state
}

// 0 - Forest, 1 - Country, 2 - City
void Java_ru_wald_1t_sound_1of_1nature_services_PlayAudioService_setEventState(JNIEnv *env, jobject thiz, jint index, jint state){
    if (state == 1) {
        eventState |= (1 << index);
    } else if (state == 0) {
        eventState &= ~(1 << index);
    }
}

void Java_ru_wald_1t_sound_1of_1nature_services_PlayAudioService_setStateCreate(JNIEnv *env, jobject thiz){

}

void Java_ru_wald_1t_sound_1of_1nature_services_PlayAudioService_setStateStart(JNIEnv *env, jobject thiz){
	gSuspendState = false;
}

void Java_ru_wald_1t_sound_1of_1nature_services_PlayAudioService_setStateStop(JNIEnv *env, jobject thiz){
	gSuspendState = true;
}

void Java_ru_wald_1t_sound_1of_1nature_services_PlayAudioService_setStateDestroy(JNIEnv *env, jobject thiz){
	gQuitState = true;
}

void Java_ru_wald_1t_sound_1of_1nature_services_PlayAudioService_main(JNIEnv *env, jobject thiz){
	gJNIEnv = env;
	gMainActivityObject = thiz;

	FMOD_Main();
}

} /* extern "C" */
