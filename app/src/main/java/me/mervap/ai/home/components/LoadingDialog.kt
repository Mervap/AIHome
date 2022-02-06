package me.mervap.ai.home.components

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import me.mervap.ai.home.R

class LoadingDialog(
  private val onCancel: (DialogFragment, DialogInterface) -> Unit = { _, _ -> },
) : DialogFragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.dialog_loading, container, false)
  }

  override fun onCancel(dialog: DialogInterface) = onCancel(this, dialog)
}