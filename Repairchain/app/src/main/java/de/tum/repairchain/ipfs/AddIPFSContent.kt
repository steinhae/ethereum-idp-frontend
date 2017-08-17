package de.tum.repairchain.ipfs

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import io.ipfs.kotlin.IPFS
import okio.Okio
import java.io.File


class AddIPFSContent : HashTextAndBarcodeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Intent.ACTION_SEND == intent.action) {
            if (intent.type != null && "text/plain" == intent.type) {
//                handleSendText(intent) // Handle text being sent
            } else {
//                AddIPFSContentPermissionsDispatcher.handleSendStreamWithCheck(this, intent)
            }
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        AddIPFSContentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults)
//    }

    fun handleSendStream(intent: Intent) {
        var uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)

        if (uri == null) {
            uri = intent.data
        }

        val inputStreamWithSource = InputStreamProvider.fromURI(this, uri)

        var createTempFile = File.createTempFile("import", null, cacheDir)

        if (inputStreamWithSource != null) {
            val sink = Okio.buffer(Okio.sink(createTempFile))

            val buffer = Okio.source(inputStreamWithSource.inputStream)
            sink.writeAll(buffer)
            sink.close()
        }

        addWithUI {
            ipfs.add.file(createTempFile)
        }
    }

    override fun getSuccessDisplayHTML()
            = "added <a href='${getSuccessURL()}'>${getSuccessURL()}</a>"

    override fun getSuccessURL() = "fs:/ipfs/${addResult!!.Hash}"
}
