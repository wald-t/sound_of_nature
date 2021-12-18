package ru.wald_t.sound_of_nature.screens.control

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_control.*
import ru.wald_t.sound_of_nature.R

class ControlFragment : Fragment() {

    private lateinit var viewModel: ControlViewModel
    private lateinit var observer: java.util.Observer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ControlViewModel::class.java]

        viewModel.liveData.observeForever {
            if (controlPlayButton != null) {
                if (it.event == "Nothing") controlPlayButton.visibility = INVISIBLE
                else controlPlayButton.visibility = VISIBLE
                if (it.state) controlPlayButton.text = "Stop"
                else controlPlayButton.text = "Start"
                controlEventText.text = it.event
            }
        }

        controlPlayButton.setOnClickListener {
            if (viewModel.mediaController.playbackState.state != PlaybackStateCompat.STATE_PLAYING) {
                viewModel.starEvent()
            } else {
                viewModel.stopEvent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.bindService()
    }

    override fun onPause() {
        super.onPause()
        viewModel.unbindService()
    }

}