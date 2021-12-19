package ru.wald_t.sound_of_nature.screens.fragments.country

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import kotlinx.android.synthetic.main.fragment_country.*
import ru.wald_t.sound_of_nature.R

class CountryFragment : Fragment() {
    private lateinit var viewModel: CountryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_country, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CountryViewModel::class.java]

        countrySpinner.adapter = viewModel.getAdapter()

        countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.setHour(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.bindService()
        countrySpinner.setSelection(viewModel.getHour())
    }

    override fun onPause() {
        super.onPause()
        viewModel.unbindService()
    }
}