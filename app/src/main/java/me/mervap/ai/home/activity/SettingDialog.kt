package me.mervap.ai.home.activity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.mervap.ai.home.R
import me.mervap.ai.home.http.*
import retrofit2.await

class SettingDialog(val client: (String) -> DataAPI) : DialogFragment() {
  private lateinit var textView: TextView
  private lateinit var textMainVentFlap: TextView
  private lateinit var textRoomVentFlap: TextView
  private lateinit var seekbarMain: SeekBar
  private lateinit var seekbarRoom: SeekBar
  private lateinit var switchMode: SwitchMaterial
  private lateinit var domain: String

  private var triggerMode = 0
  private var mainProgress = 0
  private var roomProgress = 0

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val inflater = requireActivity().layoutInflater
    val view = inflater.inflate(R.layout.settings_layout, null)
    val builder = AlertDialog.Builder(activity)
    builder.setView(view)

    domain = requireActivity().sharedPreferences.baseUrl()
    val editDomain = view.findViewById<EditText>(R.id.domain)
    editDomain.setText(domain)

    textView = view.findViewById(R.id.textView)
    textMainVentFlap = view.findViewById(R.id.text_main_vent_flap)
    textRoomVentFlap = view.findViewById(R.id.text_room_vent_flap)
    seekbarMain = view.findViewById(R.id.seekBar_main)
    seekbarRoom = view.findViewById(R.id.seekBar_room)
    switchMode = view.findViewById(R.id.switch_mode)

    textView.text = getString(R.string.settings)

    seekbarMain.setOnSeekBarChangeListener(object : DefaultOnSeekBarChangeListener() {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        textMainVentFlap.text =
          getString(R.string.main_flat, seekBar.progress * 5)
      }
    })
    seekbarRoom.setOnSeekBarChangeListener(object : DefaultOnSeekBarChangeListener() {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        textRoomVentFlap.text =
          getString(R.string.room_flat, seekBar.progress * 5)
      }
    })

    switchMode.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        seekbarMain.isEnabled = true
        seekbarRoom.isEnabled = true
      }
      else {
        seekbarMain.isEnabled = false
        seekbarRoom.isEnabled = false
      }
    }

    editDomain.addTextChangedListener {
      domain = editDomain.text.toString()

      CoroutineScope(Dispatchers.Default).launch {
        val mutex = Mutex()
        var done = false

        launch {
          getSettings(
            onFailure = {
              mutex.withLock {
                done = true
                requireActivity().runOnUiThread {
                  textMainVentFlap.text = getString(R.string.cant_get_data_settings)
                  textRoomVentFlap.text = getString(R.string.cant_get_data_settings)
                }
              }
            }
          ) {
            mutex.withLock {
              done = true
              requireActivity().runOnUiThread { setFlapProgress() }
            }
          }
        }

        delay(300)
        mutex.withLock {
          if (!done) {
            requireActivity().runOnUiThread {
              textMainVentFlap.text = getString(R.string.preaseWait)
              textRoomVentFlap.text = getString(R.string.preaseWait)
            }
          }
        }
      }
    }

    CoroutineScope(Dispatchers.Default).launch {
      getSettings {
        requireActivity().runOnUiThread {
          setFlapProgress()
        }
      }
    }

    val okButton = view.findViewById<Button>(R.id.button)
    okButton.setOnClickListener {
      textView.text = getString(R.string.waiting)
      triggerMode = if (switchMode.isChecked) 1 else 0
      mainProgress = seekbarMain.progress * 5
      roomProgress = seekbarRoom.progress * 5

      saveDomain(domain)
      (activity as? RequestActivity)?.loadData()

      CoroutineScope(Dispatchers.Default).launch {
        putSettings()
      }
      dialog?.cancel()
    }
    return builder.create()
  }

  private fun saveDomain(domain: String) {
    val editable = requireActivity().sharedPreferences.edit()
    editable.putString("url", domain)
    editable.apply()
  }

  private fun setFlapProgress() {
    switchMode.isChecked = triggerMode == 1
    seekbarMain.isEnabled = triggerMode == 1
    seekbarRoom.isEnabled = triggerMode == 1

    seekbarMain.progress = mainProgress
    seekbarRoom.progress = roomProgress
    textMainVentFlap.text = getString(R.string.main_flat, mainProgress * 5)
    textRoomVentFlap.text = getString(R.string.room_flat, roomProgress * 5)
  }

  private suspend fun getSettings(
    onFailure: suspend () -> Unit = {},
    onSuccess: suspend () -> Unit,
  ) {
    try {
      val json = client(domain).getFlapData().await()
      if (json is Result.Success<FlapInfo>) {
        val data = json.data
        mainProgress = (data.mainVentFlap / 5).toInt()
        roomProgress = (data.roomVentFlap / 5).toInt()
        triggerMode = data.triggerMode
      }
      onSuccess()
    }
    catch (e: Exception) {
      e.printStackTrace()
      onFailure()
    }
  }

  private suspend fun putSettings() {
    try {
      client(domain).putFlapData(triggerMode, mainProgress, roomProgress).await()
    }
    catch (e: Exception) {
      e.printStackTrace()
    }
  }
}

private abstract class DefaultOnSeekBarChangeListener : OnSeekBarChangeListener {
  override fun onStartTrackingTouch(p0: SeekBar?) {}
  override fun onStopTrackingTouch(p0: SeekBar?) {}
}