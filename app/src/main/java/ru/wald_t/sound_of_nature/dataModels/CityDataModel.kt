package ru.wald_t.sound_of_nature.dataModels

class CityDataModel(var traffic: Int, var walla: Int) {
    constructor() : this(0, 0)


    fun getTrafficFloat(): Float {
        return traffic.toFloat() / 100
    }

    fun getWallaFloat(): Float {
        return walla.toFloat() / 100
    }

    override fun toString(): String {
        return "CityDataModel(traffic=$traffic, walla=$walla)"
    }

}