package ru.wald_t.sound_of_nature.screens.city

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import kotlinx.android.synthetic.main.fragment_city.*
import ru.wald_t.sound_of_nature.R

class CityFragment : Fragment() {
    private lateinit var viewModel: CityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_city, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CityViewModel::class.java]

        cityParameterTraffic.progress = viewModel.getTraffic()
        cityParameterWalla.progress = viewModel.getWalla()

        cityParameterTraffic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.setTraffic(cityParameterTraffic.progress)
            }
        })

        cityParameterWalla.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.setWalla(cityParameterWalla.progress)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.bindService()

    }

    override fun onPause() {
        super.onPause()
        viewModel.unbindService()
        viewModel.savePrefs()
    }

}