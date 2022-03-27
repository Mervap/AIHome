package me.mervap.ai.home.activity

import android.content.DialogInterface
import android.os.Bundle
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.*
import me.mervap.ai.home.R
import me.mervap.ai.home.http.*
import retrofit2.await

class JalousieActivity : RequestActivity() {

  private lateinit var closedButton: Button
  private lateinit var openButton: Button
  private lateinit var changeButton: Button
  private lateinit var switchMode: SwitchMaterial
  private lateinit var beginTime: TimePicker
  private lateinit var endTime: TimePicker
  private lateinit var actualStatus: Status
  private lateinit var actualMode: Mode

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.jalousie)
    loadData()
  }

  override fun onLoadingDialogCancel(fragment: DialogFragment, dialog: DialogInterface) {}

  private fun changeIsEnabled() {
    openButton.isEnabled = false
    closedButton.isEnabled = false

    if (actualMode == Mode.MANUAL) {
      if (actualStatus == Status.OPEN) closedButton.isEnabled = true
      else openButton.isEnabled = true
    }

    val isAutoMode = (actualMode == Mode.AUTO)
    switchMode.isChecked = isAutoMode
    changeButton.isEnabled = isAutoMode
    beginTime.isEnabled = isAutoMode
    endTime.isEnabled = isAutoMode

  }

  override suspend fun loadDataImpl() {
    try {
      val currentDataResponse = client().getJalousie().await()
      if (currentDataResponse !is Result.Success<JalousieInfo>) {
        showNoDataDialog()
        return
      }
      runOnUiThread {
        loadingFragment?.dismiss()

        val currentData = currentDataResponse.data
        actualMode = currentData.mode
        actualStatus = currentData.status
        openButton = findViewById(R.id.open)
        closedButton = findViewById(R.id.closed)
        changeButton = findViewById(R.id.inputBeginTime)
        switchMode = findViewById(R.id.switchStatus)
        beginTime = findViewById(R.id.beginTime)
        endTime = findViewById(R.id.endTime)

        changeIsEnabled()

        openButton.setOnClickListener {
          actualStatus = Status.OPEN
          changeIsEnabled()
          putJalousie()
        }

        closedButton.setOnClickListener {
          actualStatus = Status.CLOSED
          changeIsEnabled()
          putJalousie()
        }

        switchMode.setOnCheckedChangeListener { _, isChecked ->
          actualMode = if (isChecked) Mode.AUTO else Mode.MANUAL
          changeIsEnabled()
          putJalousie()
        }

        val (beginHour, beginMinute) = currentData.beginTime.split(":")
        beginTime.hour = beginHour.toInt()
        beginTime.minute = beginMinute.toInt()
        beginTime.setIs24HourView(true)

        val (endHour, endMinute) = currentData.endTime.split(":")
        endTime.hour = endHour.toInt()
        endTime.minute = endMinute.toInt()
        endTime.setIs24HourView(true)

        changeButton.setOnClickListener {
          CoroutineScope(Dispatchers.Default).launch {
            updateTime()
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      showNoConnectionDialog()
    }
  }

  private fun putJalousie() {
    CoroutineScope(Dispatchers.Default).launch {
      try {
        client().putJalousie(actualMode, actualStatus).await()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  private suspend fun updateTime() {
    try {
      client().updateTime(
        "${beginTime.hour}:${beginTime.minute}",
        "${endTime.hour}:${endTime.minute}"
      ).await()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

}