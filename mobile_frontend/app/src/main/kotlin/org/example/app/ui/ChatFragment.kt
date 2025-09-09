package org.example.app.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import java.text.DateFormat
import java.util.Date

/**
 * ChatFragment provides a simple messaging UI (local only demo).
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
         * Setup list and input controls.
         */
        super.onViewCreated(view, savedInstanceState)
        val list: RecyclerView = view.findViewById(R.id.recycler_chat)
        val input: EditText = view.findViewById(R.id.input_message)
        val send: Button = view.findViewById(R.id.btn_send)

        adapter = ChatAdapter(messages)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        send.setOnClickListener {
            val t = input.text?.toString()?.trim() ?: ""
            if (!TextUtils.isEmpty(t)) {
                messages.add(Message(t, true, Date().time))
                adapter.notifyItemInserted(messages.size - 1)
                list.scrollToPosition(messages.size - 1)
                input.setText("")
                // Demo echo back
                messages.add(Message("Echo: $t", false, Date().time))
                adapter.notifyItemInserted(messages.size - 1)
                list.scrollToPosition(messages.size - 1)
            }
        }
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
