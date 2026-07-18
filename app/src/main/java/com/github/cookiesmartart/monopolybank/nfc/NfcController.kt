package com.github.cookiesmartart.monopolybank.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import java.util.concurrent.atomic.AtomicReference

/**
 * Thin wrapper around Android's reader-mode NFC API. Reader mode is used instead of foreground
 * dispatch because we only ever want to actively *scan* while a specific screen/dialog is open,
 * not receive tag intents app-wide. Only the tag's factory UID is read — nothing is written to
 * the tag, so any blank NTAG213/215/216 sticker works out of the box.
 */
class NfcController(private val activity: Activity) : NfcAdapter.ReaderCallback {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    /** Whether this device has NFC hardware at all. */
    val isHardwareAvailable: Boolean get() = nfcAdapter != null

    /** Whether NFC hardware is present and currently switched on in system settings. */
    fun isReady(): Boolean = nfcAdapter?.isEnabled == true

    // Reader mode keeps polling and can call onTagDiscovered multiple times for a single tag
    // presentation (e.g. the user holding the phone on the tag for more than one poll cycle).
    // getAndSet(null) below ensures only the first of those firings is ever delivered.
    private val pendingCallback = AtomicReference<((String) -> Unit)?>(null)

    fun startScanning(onTagScanned: (String) -> Unit) {
        pendingCallback.set(onTagScanned)
        val adapter = nfcAdapter ?: return
        val flags = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
        adapter.enableReaderMode(activity, this, flags, null)
    }

    fun stopScanning() {
        pendingCallback.set(null)
        nfcAdapter?.disableReaderMode(activity)
    }

    override fun onTagDiscovered(tag: Tag) {
        val callback = pendingCallback.getAndSet(null) ?: return
        val tagId = tag.id.toHexString()
        activity.runOnUiThread { callback(tagId) }
    }
}

private fun ByteArray.toHexString(): String = joinToString(separator = "") { byte ->
    "%02X".format(byte)
}
