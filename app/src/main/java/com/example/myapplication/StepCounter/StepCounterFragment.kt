package com.example.myapplication.StepCounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myapplication.models.StepCounterViewModel

class StepCounterFragment : Fragment() {

    private val stepCounterViewModel: StepCounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stepCounterViewModel.registerSensor()
    }

    override fun onDestroy() {
        super.onDestroy()
        stepCounterViewModel.unregisterSensor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                StepCounterScreen(stepCounterViewModel)
            }
        }
    }
}
