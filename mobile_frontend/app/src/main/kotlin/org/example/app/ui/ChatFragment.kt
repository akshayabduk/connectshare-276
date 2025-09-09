package org.example.app.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.example.app.R
import java.text.DateFormat
import java.util.Date

/**
 * ChatFragment provides a simple messaging UI, upgraded to act as a conversational AI assistant
 * with status indicators and quick prompt chips.
 */
class ChatFragment : Fragment() {

    data class Message(val text: String, val isSent: Boolean, val time: Long)

    private val messages: MutableList<Message> = mutableListOf()
    private lateinit var adapter: ChatAdapter

    // PUBLIC_INTERFACE
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * Inflate chat UI.
         */
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    // PUBLIC_INTERFACE
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /**
         * Setup list, input controls, AI header, and quick prompts.
         */
        super.onViewCreated(view, savedInstanceState)
        val list: RecyclerView = view.findViewById(R.id.recycler_chat)
        val input: EditText = view.findViewById(R.id.input_message)
        val send: Button = view.findViewById(R.id.btn_send)
        val chipPrompts: ChipGroup = view.findViewById(R.id.chip_prompts)
        val aiProgress: View = view.findViewById(R.id.ai_progress_chat)
        val aiStatusText: TextView = view.findViewById(R.id.ai_status_text_chat)

        adapter = ChatAdapter(messages)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        // Welcome message from AI on first open
        if (messages.isEmpty()) {
            addAiMessage(getString(R.string.assistant_reply_prefix) + " " + getString(R.string.ai_share_predictions_title))
        }

        // Quick prompts
        addPromptChip(chipPrompts, getString(R.string.prompt_fastest_share)) { prompt ->
            input.setText(prompt)
            send.performClick()
        }
        addPromptChip(chipPrompts, getString(R.string.prompt_recommend_targets)) { prompt ->
            input.setText(prompt)
            send.performClick()
        }
        addPromptChip(chipPrompts, getString(R.string.prompt_explain_wifi_bt)) { prompt ->
            input.setText(prompt)
            send.performClick()
        }

        // Prefill from another screen if provided
        val prefill = arguments?.getString("prefill").orEmpty()
        if (prefill.isNotBlank()) {
            input.setText(prefill)
            send.post { send.performClick() }
        }

        send.setOnClickListener {
            val t = input.text?.toString()?.trim() ?: ""
            if (!TextUtils.isEmpty(t)) {
                addUserMessage(t)
                input.setText("")

                // Simulate AI thinking...
                aiProgress.visibility = View.VISIBLE
                aiStatusText.visibility = View.VISIBLE
                view.postDelayed({
                    aiProgress.visibility = View.GONE
                    aiStatusText.visibility = View.GONE
                    val reply = "Here's a tip: Use Wi‑Fi for larger files and Bluetooth for quick device-to-device sharing. " +
                        "I can also suggest targets based on your history."
                    addAiMessage(reply)
                    list.scrollToPosition(messages.size - 1)
                }, 700)
            }
        }
    }

    private fun addPromptChip(group: ChipGroup, text: String, onChoose: (String) -> Unit) {
        val chip = Chip(requireContext()).apply {
            this.text = text
            isClickable = true
            isCheckable = false
            setOnClickListener { onChoose.invoke(text) }
        }
        group.addView(chip)
    }

    private fun addUserMessage(text: String) {
        messages.add(Message(text, true, Date().time))
        adapter.notifyItemInserted(messages.size - 1)
    }

    private fun addAiMessage(text: String) {
        messages.add(Message(text, false, Date().time))
        adapter.notifyItemInserted(messages.size - 1)
    }

    private class ChatAdapter(private val items: List<Message>) :
        RecyclerView.Adapter<ChatAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val left: android.widget.TextView = v.findViewById(R.id.left_text)
            val right: android.widget.TextView = v.findViewById(R.id.right_text)
            val timestampLeft: android.widget.TextView = v.findViewById(R.id.left_time)
            val timestampRight: android.widget.TextView = v.findViewById(R.id.right_time)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val m = items[position]
            val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(m.time))
            if (m.isSent) {
                holder.left.visibility = View.GONE
                holder.timestampLeft.visibility = View.GONE
                holder.right.visibility = View.VISIBLE
                holder.timestampRight.visibility = View.VISIBLE
                holder.right.text = m.text
                holder.timestampRight.text = time
            } else {
                holder.right.visibility = View.GONE
                holder.timestampRight.visibility = View.GONE
                holder.left.visibility = View.VISIBLE
                holder.timestampLeft.visibility = View.VISIBLE
                holder.left.text = m.text
                holder.timestampLeft.text = time
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
