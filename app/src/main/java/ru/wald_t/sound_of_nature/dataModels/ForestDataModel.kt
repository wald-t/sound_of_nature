package ru.wald_t.sound_of_nature.dataModels

class ForestDataModel(var rain: Int, var wind: Int, var cover: Int) {
    constructor() : this(0, 0, 0)


    fun getRainFloat(): Float {
        return rain.toFloat() / 100
    }

    fun getWindFloat(): Float {
        return wind.toFloat() / 100
    }

    fun getCoverFloat(): Float {
        return cover.toFloat() * 360 - 180
    }

    override fun toString(): String {
        return "ForestDataModel(rain=$rain, wind=$wind, cover=$cover)"
    }


}