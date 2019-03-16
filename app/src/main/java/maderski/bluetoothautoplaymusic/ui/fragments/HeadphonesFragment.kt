package maderski.bluetoothautoplaymusic.ui.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.CompoundButtonCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

import java.util.HashSet

import maderski.bluetoothautoplaymusic.utils.BluetoothUtils
import maderski.bluetoothautoplaymusic.BuildConfig
import maderski.bluetoothautoplaymusic.R
import maderski.bluetoothautoplaymusic.sharedprefs.BAPMPreferences
import maderski.bluetoothautoplaymusic.bus.BusProvider
import maderski.bluetoothautoplaymusic.bus.events.A2DPSetSwitchEvent

class HeadphonesFragment : DialogFragment() {

    private val removedDevices = HashSet<String>()

    private var mListener: OnFragmentInteractionListener? = null

    private val nonHeadphoneDevices: Set<String>
        get() {
            val btDevices = BAPMPreferences.getBTDevices(requireActivity())
            val headphoneDevices = BAPMPreferences.getHeadphoneDevices(requireActivity())

            for (headphoneDevice in headphoneDevices) {
                if (btDevices.contains(headphoneDevice)) {
                    btDevices.remove(headphoneDevice)
                }
            }

            return btDevices
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_headphones, container, false)
        checkboxCreator(rootView)

        val audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volumeSeekBar = rootView.findViewById<View>(R.id.volume_seekBar) as SeekBar
        volumeSeekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.progress = BAPMPreferences.getHeadphonePreferredVolume(requireActivity())
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                BAPMPreferences.setHeadphonePreferredVolume(requireActivity(), progress)
                Log.d(TAG, "Progress: " + Integer.toString(progress))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        val doneButton = rootView.findViewById<View>(R.id.autoplay_done) as Button
        doneButton.setOnClickListener {
            mListener?.headphonesDoneClicked(removedDevices)
            dismiss()
        }

        val a2dpSwitch = rootView.findViewById<View>(R.id.sw_headphones_a2dp) as Switch
        val useA2dp = BAPMPreferences.getUseA2dpHeadphones(requireActivity())
        a2dpSwitch.isChecked = useA2dp
        a2dpSwitch.setOnCheckedChangeListener { buttonView, isChecked -> BusProvider.busInstance.post(A2DPSetSwitchEvent(isChecked)) }

        removedDevices?.clear()
        return rootView
    }

    //Create Checkboxes
    private fun checkboxCreator(view: View) {

        var checkBox: CheckBox
        val textView: TextView

        val autoplayCkBoxLL = view.findViewById<View>(R.id.autoplay_only_ll) as LinearLayout
        autoplayCkBoxLL.removeAllViews()
        val listOfBTDevices = BluetoothUtils.listOfBluetoothDevices(requireActivity())
        if (listOfBTDevices.contains("No Bluetooth Device found") || listOfBTDevices.isEmpty()) {
            textView = TextView(activity)
            textView.setText(R.string.no_BT_found)
            autoplayCkBoxLL.addView(textView)
        } else {
            for (BTDevice in listOfBTDevices) {
                var textColor = R.color.colorPrimary
                checkBox = CheckBox(activity)
                checkBox.text = BTDevice
                if (nonHeadphoneDevices.contains(BTDevice)) {
                    textColor = R.color.lightGray
                    val states = arrayOf(intArrayOf(android.R.attr.state_checked))
                    val colors = intArrayOf(textColor, textColor)
                    CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList(states, colors))
                    checkBox.isClickable = false
                    checkBox.isChecked = true
                } else if (BAPMPreferences.getBTDevices(requireActivity()).isNotEmpty()) {
                    checkBox.isChecked = BAPMPreferences.getHeadphoneDevices(requireActivity()).contains(BTDevice)
                }
                checkBox.setTextColor(ContextCompat.getColor(requireActivity(), textColor))
                checkBox.typeface = Typeface.createFromAsset(requireActivity().assets, "fonts/TitilliumText400wt.otf")

                if (!nonHeadphoneDevices.contains(BTDevice)) {
                    checkboxListener(view.context, checkBox, BTDevice)
                }
                autoplayCkBoxLL.addView(checkBox)
            }
        }

    }

    //Get Selected Checkboxes
    private fun checkboxListener(context: Context, checkBox: CheckBox, BTDevice: String) {

        checkBox.setOnClickListener {
            val savedHeadphoneDevices = HashSet(BAPMPreferences.getHeadphoneDevices(context))
            if (checkBox.isChecked) {
                savedHeadphoneDevices.add(BTDevice)
                if (removedDevices.contains(BTDevice)) {
                    removedDevices.remove(BTDevice)
                }
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "TRUE $BTDevice")
                    Log.i(TAG, "SAVED")
                }
            } else {
                savedHeadphoneDevices.remove(BTDevice)
                removedDevices.add(BTDevice)
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "FALSE $BTDevice")
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "SAVED")
            }
            mListener?.setHeadphoneDevices(savedHeadphoneDevices)
            mListener?.headDeviceSelection(BTDevice, checkBox.isChecked)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        fun setHeadphoneDevices(headphoneDevices: HashSet<String>)
        fun headphonesDoneClicked(removedDevices: HashSet<String>)
        fun headDeviceSelection(deviceName: String, addDevice: Boolean)
    }

    companion object {

        private const val TAG = "HeadphonesFragment"

        fun newInstance(): HeadphonesFragment {
            return HeadphonesFragment()
        }
    }
}