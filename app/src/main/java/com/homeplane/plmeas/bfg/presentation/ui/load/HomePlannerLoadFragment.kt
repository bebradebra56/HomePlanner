package com.homeplane.plmeas.bfg.presentation.ui.load

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.homeplane.plmeas.MainActivity
import com.homeplane.plmeas.R
import com.homeplane.plmeas.bfg.data.shar.HomePlannerSharedPreference
import com.homeplane.plmeas.databinding.FragmentLoadHomePlannerBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomePlannerLoadFragment : Fragment(R.layout.fragment_load_home_planner) {
    private lateinit var homePlannerLoadBinding: FragmentLoadHomePlannerBinding

    private val homePlannerLoadViewModel by viewModel<HomePlannerLoadViewModel>()

    private val homePlannerSharedPreference by inject<HomePlannerSharedPreference>()

    private var homePlannerUrl = ""

    private val homePlannerRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        homePlannerSharedPreference.homePlannerNotificationState = 2
        homePlannerNavigateToSuccess(homePlannerUrl)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homePlannerLoadBinding = FragmentLoadHomePlannerBinding.bind(view)

        homePlannerLoadBinding.homePlannerGrandButton.setOnClickListener {
            val homePlannerPermission = Manifest.permission.POST_NOTIFICATIONS
            homePlannerRequestNotificationPermission.launch(homePlannerPermission)
        }

        homePlannerLoadBinding.homePlannerSkipButton.setOnClickListener {
            homePlannerSharedPreference.homePlannerNotificationState = 1
            homePlannerSharedPreference.homePlannerNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            homePlannerNavigateToSuccess(homePlannerUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homePlannerLoadViewModel.homePlannerHomeScreenState.collect {
                    when (it) {
                        is HomePlannerLoadViewModel.HomePlannerHomeScreenState.HomePlannerLoading -> {

                        }

                        is HomePlannerLoadViewModel.HomePlannerHomeScreenState.HomePlannerError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is HomePlannerLoadViewModel.HomePlannerHomeScreenState.HomePlannerSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val homePlannerNotificationState = homePlannerSharedPreference.homePlannerNotificationState
                                when (homePlannerNotificationState) {
                                    0 -> {
                                        homePlannerLoadBinding.homePlannerNotiGroup.visibility = View.VISIBLE
                                        homePlannerLoadBinding.homePlannerLoadingGroup.visibility = View.GONE
                                        homePlannerUrl = it.data
                                    }
                                    1 -> {
                                        if (System.currentTimeMillis() / 1000 > homePlannerSharedPreference.homePlannerNotificationRequest) {
                                            homePlannerLoadBinding.homePlannerNotiGroup.visibility = View.VISIBLE
                                            homePlannerLoadBinding.homePlannerLoadingGroup.visibility = View.GONE
                                            homePlannerUrl = it.data
                                        } else {
                                            homePlannerNavigateToSuccess(it.data)
                                        }
                                    }
                                    2 -> {
                                        homePlannerNavigateToSuccess(it.data)
                                    }
                                }
                            } else {
                                homePlannerNavigateToSuccess(it.data)
                            }
                        }

                        HomePlannerLoadViewModel.HomePlannerHomeScreenState.HomePlannerNotInternet -> {
                            homePlannerLoadBinding.homePlannerStateGroup.visibility = View.VISIBLE
                            homePlannerLoadBinding.homePlannerLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun homePlannerNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_homePlannerLoadFragment_to_homePlannerV,
            bundleOf(HOME_PLANNER_D to data)
        )
    }

    companion object {
        const val HOME_PLANNER_D = "homePlannerData"
    }
}