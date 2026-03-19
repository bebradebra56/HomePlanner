package com.homeplane.plmeas.bfg.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.homeplane.plmeas.bfg.presentation.app.HomePlannerApplication
import com.homeplane.plmeas.bfg.presentation.ui.load.HomePlannerLoadFragment
import org.koin.android.ext.android.inject

class HomePlannerV : Fragment(){

    private lateinit var homePlannerPhoto: Uri
    private var homePlannerFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val homePlannerTakeFile: ActivityResultLauncher<PickVisualMediaRequest> = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        homePlannerFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
        homePlannerFilePathFromChrome = null
    }

    private val homePlannerTakePhoto: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            homePlannerFilePathFromChrome?.onReceiveValue(arrayOf(homePlannerPhoto))
            homePlannerFilePathFromChrome = null
        } else {
            homePlannerFilePathFromChrome?.onReceiveValue(null)
            homePlannerFilePathFromChrome = null
        }
    }

    private val homePlannerDataStore by activityViewModels<HomePlannerDataStore>()


    private val homePlannerViFun by inject<HomePlannerViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (homePlannerDataStore.homePlannerView.canGoBack()) {
                        homePlannerDataStore.homePlannerView.goBack()
                        Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "WebView can go back")
                    } else if (homePlannerDataStore.homePlannerViList.size > 1) {
                        Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "WebView can`t go back")
                        homePlannerDataStore.homePlannerViList.removeAt(homePlannerDataStore.homePlannerViList.lastIndex)
                        Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "WebView list size ${homePlannerDataStore.homePlannerViList.size}")
                        homePlannerDataStore.homePlannerView.destroy()
                        val previousWebView = homePlannerDataStore.homePlannerViList.last()
                        homePlannerAttachWebViewToContainer(previousWebView)
                        homePlannerDataStore.homePlannerView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (homePlannerDataStore.homePlannerIsFirstCreate) {
            homePlannerDataStore.homePlannerIsFirstCreate = false
            homePlannerDataStore.homePlannerContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return homePlannerDataStore.homePlannerContainerView
        } else {
            return homePlannerDataStore.homePlannerContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "onViewCreated")
        if (homePlannerDataStore.homePlannerViList.isEmpty()) {
            homePlannerDataStore.homePlannerView = HomePlannerVi(requireContext(), object :
                HomePlannerCallBack {
                override fun homePlannerHandleCreateWebWindowRequest(homePlannerVi: HomePlannerVi) {
                    homePlannerDataStore.homePlannerViList.add(homePlannerVi)
                    Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "WebView list size = ${homePlannerDataStore.homePlannerViList.size}")
                    Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "CreateWebWindowRequest")
                    homePlannerDataStore.homePlannerView = homePlannerVi
                    homePlannerVi.homePlannerSetFileChooserHandler { callback ->
                        homePlannerHandleFileChooser(callback)
                    }
                    homePlannerAttachWebViewToContainer(homePlannerVi)
                }

            }, homePlannerWindow = requireActivity().window).apply {
                homePlannerSetFileChooserHandler { callback ->
                    homePlannerHandleFileChooser(callback)
                }
            }
            homePlannerDataStore.homePlannerView.homePlannerFLoad(arguments?.getString(
                HomePlannerLoadFragment.HOME_PLANNER_D) ?: "")
//            ejvview.fLoad("www.google.com")
            homePlannerDataStore.homePlannerViList.add(homePlannerDataStore.homePlannerView)
            homePlannerAttachWebViewToContainer(homePlannerDataStore.homePlannerView)
        } else {
            homePlannerDataStore.homePlannerViList.forEach { webView ->
                webView.homePlannerSetFileChooserHandler { callback ->
                    homePlannerHandleFileChooser(callback)
                }
            }
            homePlannerDataStore.homePlannerView = homePlannerDataStore.homePlannerViList.last()

            homePlannerAttachWebViewToContainer(homePlannerDataStore.homePlannerView)
        }
        Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "WebView list size = ${homePlannerDataStore.homePlannerViList.size}")
    }

    private fun homePlannerHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "handleFileChooser called, callback: ${callback != null}")

        homePlannerFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Launching file picker")
                    homePlannerTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                1 -> {
                    Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Launching camera")
                    homePlannerPhoto = homePlannerViFun.homePlannerSavePhoto()
                    homePlannerTakePhoto.launch(homePlannerPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "File chooser canceled")
                callback?.onReceiveValue(null)
                homePlannerFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun homePlannerAttachWebViewToContainer(w: HomePlannerVi) {
        homePlannerDataStore.homePlannerContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            homePlannerDataStore.homePlannerContainerView.removeAllViews()
            homePlannerDataStore.homePlannerContainerView.addView(w)
        }
    }


}