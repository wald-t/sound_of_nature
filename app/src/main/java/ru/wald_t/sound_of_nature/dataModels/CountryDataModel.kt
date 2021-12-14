package ru.wald_t.sound_of_nature.dataModels

class CountryDataModel(var hour: Int) {
    constructor(): this(0)

    fun getHourFloat(): Float{
        return hour.toFloat()
    }

    override fun toString(): String {
        return "CountryDataModel(hour=$hour)"
    }
}