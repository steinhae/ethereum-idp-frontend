package de.tum.repairchain.ipfs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import de.tum.repairchain.R
import de.tum.repairchain.UploadImage
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.model.NamedHash
import kotlinx.android.synthetic.main.activity_add.*
import net.steamcrafted.loadtoast.LoadToast
import java.net.ConnectException
import javax.inject.Inject
import de.tum.repairchain.Constants.*;

abstract class HashTextAndBarcodeActivity : AppCompatActivity() {

    @Inject
    lateinit var ipfs: IPFS

    var addResult: NamedHash? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("the hast activity", this.toString())

        App.component().inject(this)
        setContentView(R.layout.activity_add)

        hashInfoText.movementMethod = LinkMovementMethod.getInstance()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.copy -> {
                val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager;
                val clip = ClipData.newPlainText("hash", addResult?.Hash);
                clipboardManager.primaryClip = clip;

                Snackbar.make(hashInfoText, "copy " + addResult?.Hash, Snackbar.LENGTH_LONG).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun addWithUI(callback: () -> NamedHash?) {

        val show = LoadToast(this).show()

        Thread(Runnable {
            try {
                addResult = callback()
            } catch (e: ConnectException) {

                addResult = null
            }
            runOnUiThread {
                val displayString: String
                if (addResult == null) {
                    show.error()
                    displayString = "could not execute ( daemon running? )" + ipfs.lastError?.Message
                } else {
                    show.success()
                    var successString = getSuccessDisplayHTML()
                    Log.i("SuccessString", successString)
                    displayString = successString

                }

                // Necessary because SDK versions 24 and up have deprecated fromHtml(String)
                @SuppressWarnings("deprecation")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    hashInfoText.text = Html.fromHtml(displayString, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    hashInfoText.text = Html.fromHtml(displayString)
                }

                var successUrl = getSuccessURL()
                Log.i("just for assurance", successUrl)
                var intent = Intent()
                intent.action = RETURN_HASH
                intent.putExtra(RETURN_IMAGE_URL, successUrl)
                setResult(RETURN_URL, intent)
                finish()

            }
        }).start()

    }

    abstract fun getSuccessDisplayHTML(): String
    abstract fun getSuccessURL(): String
}
