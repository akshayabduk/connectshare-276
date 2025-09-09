package org.example.app.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.data.HistoryRepository
import java.text.DateFormat
import java.util.Date

/**
 * ShareFragment allows users to pick files and share them via Bluetooth or Wi‑Fi.
 * Actual transport is delegated to Android's share intents as a secure baseline.
 */
class ShareFragment : Fragment() {

    private val selectedUris: MutableList<Uri> = mutableListOf()
    private lateinit var filesList: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: SelectedFilesAdapter

    // PUBLIC_INTERFACE
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * Inflate the share UI.
         */
        return inflater.inflate(R.layout.fragment_share, container, false)
    }

    // PUBLIC_INTERFACE
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /**
         * Wire up buttons and list.
         */
        super.onViewCreated(view, savedInstanceState)
        val pick: Button = view.findViewById(R.id.btn_pick)
        val sendBt: Button = view.findViewById(R.id.btn_send_bluetooth)
        val sendWifi: Button = view.findViewById(R.id.btn_send_wifi)
        filesList = view.findViewById(R.id.recycler_selected)
        emptyText = view.findViewById(R.id.empty_selection)

        adapter = SelectedFilesAdapter(selectedUris) { uri ->
            selectedUris.remove(uri)
            updateListState()
        }
        filesList.layoutManager = LinearLayoutManager(requireContext())
        filesList.adapter = adapter

        pick.setOnClickListener { openFilePicker() }
        sendBt.setOnClickListener { confirmAndShare("Bluetooth") { shareViaBluetooth() } }
        sendWifi.setOnClickListener { confirmAndShare("Wi‑Fi") { shareViaWifi() } }

        updateListState()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            type = "*/*"
        }
        startActivityForResult(intent, REQ_PICK_FILES)
    }

    private fun confirmAndShare(method: String, action: () -> Unit) {
        if (selectedUris.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_selection, Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_send_title)
            .setMessage(getString(R.string.confirm_send_message))
            .setPositiveButton(R.string.yes) { _, _ ->
                action.invoke()
                // Log to history
                val hr = HistoryRepository(requireContext())
                selectedUris.forEach { uri ->
                    hr.addShareRecord(
                        fileNameFromUri(requireContext(), uri),
                        method,
                        Date().time
                    )
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun shareViaBluetooth() {
        shareSelectedFiles("com.android.bluetooth")
    }

    private fun shareViaWifi() {
        // As a baseline, use chooser without forcing a package.
        shareSelectedFiles(null)
    }

    private fun shareSelectedFiles(packageName: String?) {
        try {
            if (selectedUris.size == 1) {
                val uri = selectedUris.first()
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = guessMimeType(uri)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (packageName != null) setPackage(packageName)
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share_with)))
            } else {
                val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "*/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(selectedUris))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (packageName != null) setPackage(packageName)
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share_title)))
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No compatible app found", Toast.LENGTH_LONG).show()
        }
    }

    private fun guessMimeType(uri: Uri): String {
        val cR = requireContext().contentResolver
        val type = cR.getType(uri)
        if (type != null) return type
        val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
    }

    private fun updateListState() {
        adapter.notifyDataSetChanged()
        if (selectedUris.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            filesList.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            filesList.visibility = View.VISIBLE
        }
    }

    private fun fileNameFromUri(context: Context, uri: Uri): String {
        var name = "file"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && it.moveToFirst()) {
                name = it.getString(index)
            }
        }
        return name
    }

    // PUBLIC_INTERFACE
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /**
         * Handle file picker result, storing selected URIs.
         */
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PICK_FILES && resultCode == Activity.RESULT_OK) {
            selectedUris.clear()
            data?.data?.let { selectedUris.add(it) }
            val clip = data?.clipData
            if (clip != null) {
                for (i in 0 until clip.itemCount) {
                    val u = clip.getItemAt(i).uri
                    selectedUris.add(u)
                }
            }
            updateListState()
        }
    }

    private class SelectedFilesAdapter(
        private val items: List<Uri>,
        val onRemove: (Uri) -> Unit
    ) : RecyclerView.Adapter<SelectedFilesAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val name: TextView = v.findViewById(R.id.file_name)
            val remove: View = v.findViewById(R.id.btn_remove)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_selected_file, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val uri = items[position]
            holder.name.text = uri.lastPathSegment ?: uri.toString()
            holder.remove.setOnClickListener { onRemove(uri) }
        }

        override fun getItemCount(): Int = items.size
    }

    companion object {
        private const val REQ_PICK_FILES = 5001
    }
}
