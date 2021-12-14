package ru.wald_t.sound_of_nature.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.wald_t.sound_of_nature.R
import kotlinx.android.synthetic.main.fragment_main.*
import ru.wald_t.sound_of_nature.activities.MainActivity

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onStart() {
        super.onStart()
        start_forest_btn.setOnClickListener {
            (activity as MainActivity).navController.navigate(R.id.action_main_to_forestFragment)
        }
        start_country_btn.setOnClickListener {
            (activity as MainActivity).navController.navigate(R.id.action_main_to_countryFragment)
        }
        start_city_btn.setOnClickListener {
            (activity as MainActivity).navController.navigate(R.id.action_main_to_cityFragment)
        }
    }

}