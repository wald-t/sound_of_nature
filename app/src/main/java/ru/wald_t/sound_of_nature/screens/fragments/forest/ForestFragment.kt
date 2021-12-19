package ru.wald_t.sound_of_nature.screens.fragments.forest

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import kotlinx.android.synthetic.main.fragment_forest.*
import ru.wald_t.sound_of_nature.R

class ForestFragment : Fragment() {
    private lateinit var viewModel: ForestViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ForestViewModel::class.java]

        forestParameterRain.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.setRain(forestParameterRain.progress)
            }
        })

        forestParameterWind.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.setWind(forestParameterWind.progress)
            }
        })

        forestParameterCover.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.setCover(forestParameterCover.progress)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.bindService()
        forestParameterRain.progress = viewModel.getRain()
        forestParameterWind.progress = viewModel.getWind()
        forestParameterCover.progress = viewModel.getCover()
    }

    override fun onPause() {
        super.onPause()
        viewModel.unbindService()
    }
}