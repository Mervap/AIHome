package me.mervap.ai.home.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import me.mervap.ai.home.R
import me.mervap.ai.home.http.DataAPI
import me.mervap.ai.home.http.FullInfo
import me.mervap.ai.home.http.Result
import retrofit2.await

class BuildingGraphsLast24Hour : BuildingGraphs() {

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.graphs_activity)
    loadData()
  }

  override fun onLoadingDialogCancel(fragment: DialogFragment, dialog: DialogInterface) {
    val intent = Intent(fragment.activity, MainActivity::class.java)
    startActivity(intent)
  }

  override suspend fun getData(client: DataAPI): Result<List<FullInfo>> {
    return client.getLastData().await()
  }

  override fun onRequestFailure() {
    showNoDataDialog()
  }
}