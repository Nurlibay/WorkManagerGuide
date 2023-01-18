package uz.nurlibaydev.workmanagerguide.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.work.*
import coil.compose.rememberImagePainter
import uz.nurlibaydev.workmanagerguide.utils.WorkerKeys
import uz.nurlibaydev.workmanagerguide.utils.theme.WorkManagerGuideTheme
import uz.nurlibaydev.workmanagerguide.workers.ColorFilterWorker
import uz.nurlibaydev.workmanagerguide.workers.DownloadWorker

class MainActivity : ComponentActivity() {

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>().setConstraints(
            Constraints.Builder().setRequiredNetworkType(
                NetworkType.CONNECTED
            ).build()
        ).build()

        val colorFilterRequest = OneTimeWorkRequestBuilder<ColorFilterWorker>().build()

        val workManager = WorkManager.getInstance(applicationContext)

        setContent {
            WorkManagerGuideTheme {
                val workInfos = workManager.getWorkInfosForUniqueWorkLiveData("download").observeAsState().value

                val downloadInfo = remember(key1 = workInfos) {
                    workInfos?.find { it.id == downloadRequest.id }
                }

                val filterInfo = remember(key1 = workInfos) {
                    workInfos?.find { it.id == colorFilterRequest.id }
                }

                val imageUri by derivedStateOf {
                    val downloadUri = downloadInfo?.outputData?.getString(WorkerKeys.IMG_URI)?.toUri()

                    val filterUri = filterInfo?.outputData?.getString(WorkerKeys.FILTER_URI)?.toUri()

                    filterUri ?: downloadUri
                }

                Column(
                    modifier = Modifier.background(Color(0xFFBBB5B5)).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    imageUri?.let { uri ->
                        Image(
                            painter = rememberImagePainter(
                                data = uri
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(onClick = {
                        workManager.beginUniqueWork(
                            "download",
                            ExistingWorkPolicy.KEEP,
                            downloadRequest
                        )
                            .then(colorFilterRequest)
                            .enqueue()
                    }, enabled = downloadInfo?.state != WorkInfo.State.RUNNING) {
                        Text(text = "Start download")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    when (downloadInfo?.state) {
                        WorkInfo.State.RUNNING -> Text(text = "Downloading...")
                        WorkInfo.State.SUCCEEDED -> Text(text = "Download succeeded")
                        WorkInfo.State.FAILED -> Text(text = "Download failed")
                        WorkInfo.State.ENQUEUED -> Text(text = "Download enqueued")
                        WorkInfo.State.BLOCKED -> Text(text = "Download blocked")
                        else -> {}
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    when (filterInfo?.state) {
                        WorkInfo.State.RUNNING -> Text(text = "Applying filter...")
                        WorkInfo.State.SUCCEEDED -> Text(text = "Filter succeeded")
                        WorkInfo.State.FAILED -> Text(text = "Filter failed")
                        WorkInfo.State.ENQUEUED -> Text(text = "Filter enqueued")
                        WorkInfo.State.BLOCKED -> Text(text = "Filter blocked")
                        else -> {}
                    }
                }
            }
        }
    }
}