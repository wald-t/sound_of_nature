/*==============================================================================
Simple Event Example
Copyright (c), Firelight Technologies Pty, Ltd 2012-2021.

This example demonstrates the various ways of playing an event.

#### Explosion Event ####
This event is played as a one-shot and released immediately after it has been
created.

#### Looping Ambience Event ####
A single instance is started or stopped based on user input.

#### Cancel Event ####
This instance is started and if already playing, restarted.

==============================================================================*/
#include "fmod_studio.hpp"
#include "fmod.hpp"
#include "common.h"
#include <jni.h>

int FMOD_Main()
{
    void *extraDriverData = NULL;
    Common_Init(&extraDriverData);

    FMOD::Studio::System* system = NULL;
    ERRCHECK( FMOD::Studio::System::create(&system) );

    // The example Studio project is authored for 5.1 sound, so set up the system output mode to match
    FMOD::System* coreSystem = NULL;
    ERRCHECK( system->getCoreSystem(&coreSystem) );
    ERRCHECK( coreSystem->setSoftwareFormat(0, FMOD_SPEAKERMODE_5POINT1, 0) );

    ERRCHECK( system->initialize(1024, FMOD_STUDIO_INIT_NORMAL, FMOD_INIT_NORMAL, extraDriverData) );
    
    FMOD::Studio::Bank* masterBank = NULL;
    ERRCHECK( system->loadBankFile(Common_MediaPath("Master.bank"), FMOD_STUDIO_LOAD_BANK_NORMAL, &masterBank) );

    FMOD::Studio::Bank* stringsBank = NULL;
    ERRCHECK( system->loadBankFile(Common_MediaPath("Master.strings.bank"), FMOD_STUDIO_LOAD_BANK_NORMAL, &stringsBank) );

    FMOD::Studio::Bank* sfxBank = NULL;
    ERRCHECK( system->loadBankFile(Common_MediaPath("SFX.bank"), FMOD_STUDIO_LOAD_BANK_NORMAL, &sfxBank) );

    // Get the Looping Ambience event
    FMOD::Studio::EventDescription* countryDescription = NULL;
    ERRCHECK( system->getEvent("event:/Ambience/Country", &countryDescription) );
    
    FMOD::Studio::EventInstance* countryInstance = NULL;
    ERRCHECK( countryDescription->createInstance(&countryInstance) );

    FMOD::Studio::EventDescription* cityDescription = NULL;
    ERRCHECK( system->getEvent("event:/Ambience/City", &cityDescription) );

    FMOD::Studio::EventInstance* cityInstance = NULL;
    ERRCHECK( cityDescription->createInstance(&cityInstance) );

    FMOD::Studio::EventDescription* forestDescription = NULL;
    ERRCHECK( system->getEvent("event:/Ambience/Forest", &forestDescription) );

    FMOD::Studio::EventInstance* forestInstance = NULL;
    ERRCHECK( forestDescription->createInstance(&forestInstance) );

    do
    {
        if (Common_ParameterIsChanged(0)) {
            ERRCHECK(forestInstance->setParameterByName("Rain", Common_ForestGetRainParameter()));
            ERRCHECK(forestInstance->setParameterByName("Wind", Common_ForestGetWindParameter()));
            ERRCHECK(forestInstance->setParameterByName("Cover", Common_ForestGetCoverParameter()));
        }

        if (Common_ParameterIsChanged(1)) {
            ERRCHECK(countryInstance->setParameterByName("Hour", Common_CountryGetParameter()));
        }

        if (Common_ParameterIsChanged(2)) {
            ERRCHECK(cityInstance->setParameterByName("Traffic", Common_CityGetTrafficParameter()));
            ERRCHECK(cityInstance->setParameterByName("Walla", Common_CityGetWallaParameter()));
        }

        if (Common_EventIsChanged()) {
            if (Common_EventState(0)){
                ERRCHECK( forestInstance->start() );
            } else {
                ERRCHECK( forestInstance->stop(FMOD_STUDIO_STOP_IMMEDIATE) );
            }
            if (Common_EventState(1)){
                ERRCHECK( countryInstance->start() );
            } else {
                ERRCHECK( countryInstance->stop(FMOD_STUDIO_STOP_IMMEDIATE) );
            }
            if (Common_EventState(2)){
                ERRCHECK( cityInstance->start() );
            } else {
                ERRCHECK( cityInstance->stop(FMOD_STUDIO_STOP_IMMEDIATE) );
            }
        }

        Common_Update();
        ERRCHECK( system->update() );

        Common_Sleep(50);
    } while (!Common_QuitState());
    
    ERRCHECK( sfxBank->unload() );
    ERRCHECK( stringsBank->unload() );
    ERRCHECK( masterBank->unload() );

    ERRCHECK( system->release() );

    Common_Close();

    return 0;
}

