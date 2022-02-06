package me.mervap.ai.home.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.*
import me.mervap.ai.home.R
import me.mervap.ai.home.components.LoadingDialog
import me.mervap.ai.home.http.client
import java.util.*
import kotlin.system.exitProcess

abstract class RequestActivity : AppCompatActivity() {
  protected var loadingFragment: DialogFragment? = null
  private var activeRequest: Deferred<*>? = null

  override fun onDestroy() {
    runBlocking { activeRequest?.cancelAndJoin() }
    super.onDestroy()
  }

  override fun getResources(): Resources {
    val resources = super.getResources()
    resources.configuration.setLocale(Locale.US)
    return resources
  }

  fun loadData() {
    loadingFragment?.dismiss()
    val dialog = LoadingDialog { fragment, dialog -> onLoadingDialogCancel(fragment, dialog) }
    loadingFragment = dialog
    dialog.show(supportFragmentManager, "Loading")

    activeRequest?.cancel()
    activeRequest = CoroutineScope(Dispatchers.Default).async {
      loadDataImpl()
    }
  }

  abstract fun onLoadingDialogCancel(fragment: DialogFragment, dialog: DialogInterface)

  protected abstract suspend fun loadDataImpl()

  protected fun showNoConnectionDialog() {
    runOnUiThread {
      loadingFragment?.dismiss()
      val builder = AlertDialog.Builder(this)
      builder.setTitle(getString(R.string.attention_title))
        .setMessage(getString(R.string.no_serser_connection_message))
        .setCancelable(false)
        .setPositiveButton(getString(R.string.repeat_rutton)) { dialog, _ ->
          dialog.cancel()
          loadData()
        }
        .setNeutralButton(getString(R.string.settings_button)) { dialog, _ ->
          dialog.cancel()
          val manager = supportFragmentManager
          val myDialogFragment = SettingDialog(::client)
          myDialogFragment.show(manager, "SettingDialog")
        }
        .setNegativeButton(getString(R.string.exit_button)) { dialog, _ ->
          dialog.cancel()
          exitProcess(0)
        }
      val alert = builder.create()
      alert.show()
    }
  }

  protected fun showNoDataDialog() {
    runOnUiThread {
      loadingFragment?.dismiss()
      val builder = AlertDialog.Builder(this)
      builder.setTitle(getString(R.string.attention_title))
        .setMessage(getString(R.string.no_data_message))
        .setCancelable(false)
        .setPositiveButton(getString(R.string.repeat_rutton)) { dialog, _ ->
          dialog.cancel()
          loadData()
        }
        .setNegativeButton(getString(R.string.exit_button)) { dialog, _ ->
          dialog.cancel()
          exitProcess(0)
        }
      val alert = builder.create()
      alert.show()
    }
  }
}