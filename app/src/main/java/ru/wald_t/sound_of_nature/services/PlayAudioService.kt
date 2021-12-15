package ru.wald_t.sound_of_nature.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.fmod.FMOD
import android.os.Binder
import androidx.lifecycle.MutableLiveData
import ru.wald_t.sound_of_nature.BuildConfig
import ru.wald_t.sound_of_nature.dataModels.CityDataModel
import ru.wald_t.sound_of_nature.dataModels.ControlDataModel
import ru.wald_t.sound_of_nature.dataModels.CountryDataModel
import ru.wald_t.sound_of_nature.dataModels.ForestDataModel

class PlayAudioService : Service() {
    private val mBinder: IBinder = MyBinder()
    private var event: String = "Nothing"
    private var isStarted = false
    var liveData = MutableLiveData<ControlDataModel>()

    private val mThread = object : Thread(){
        override fun run() {
            main()
        }
    }

    inner class MyBinder : Binder() {
        fun getService() : PlayAudioService{
            return this@PlayAudioService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        FMOD.init(this)
        mThread.start()
        setStateCreate()
        liveData.value = ControlDataModel(event, isStarted)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setStateStart()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        setStateDestroy()
        try {
            mThread.join()
        } catch (e: InterruptedException) {}
        FMOD.close()
    }

    fun setEvent(event: String) {
        if (event != this.event || !isStarted){
            when(event){
                "Forest" -> {
                    setEventState(0, 1)
                    setEventState(1, 0)
                    setEventState(2, 0)
                    this.event = event
                    isStarted = true
                }
                "Country" -> {
                    setEventState(0, 0)
                    setEventState(1, 1)
                    setEventState(2, 0)
                    this.event = event
                    isStarted = true
                }
                "City" -> {
                    setEventState(0, 0)
                    setEventState(1, 0)
                    setEventState(2, 1)
                    this.event = event
                    isStarted = true
                }
            }
            liveData.value = ControlDataModel(event, isStarted)
        }

    }

    fun stopEvent() {
        setEventState(0, 0)
        setEventState(1, 0)
        setEventState(2, 0)
        isStarted = false
        liveData.value = ControlDataModel(event, isStarted)
    }

    fun startLastEvent() {
        setEvent(event)
    }

    fun setParameter(forestDataModel: ForestDataModel) {
        forestSetParameter(
            forestDataModel.getRainFloat(),
            forestDataModel.getWindFloat(),
            forestDataModel.getCoverFloat())
    }

    fun setParameter(countryDataModel: CountryDataModel) {
        countrySetParameter(countryDataModel.getHourFloat())
    }

    fun setParameter(cityDataModel: CityDataModel) {
        citySetParameter(
            cityDataModel.getTrafficFloat(),
            cityDataModel.getWallaFloat())
    }


    private external fun getEventState(): Int
    private external fun setEventState(event: Int, state: Int)
    external fun getButtonLabel(index: Int): String?
    external fun buttonDown(index: Int)
    external fun buttonUp(index: Int)
    private external fun forestSetParameter(rain: Float, wind: Float, cover: Float)
    private external fun countrySetParameter(index: Float)
    private external fun citySetParameter(traffic: Float, walla: Float)
    private external fun setStateCreate()
    private external fun setStateStart()
    private external fun setStateStop()
    private external fun setStateDestroy()
    private external fun main()

    init {
        for (lib:String in BuildConfig.FMOD_LIBS)
        {
            System.loadLibrary(lib)
        }
        System.loadLibrary("example")
    }


}