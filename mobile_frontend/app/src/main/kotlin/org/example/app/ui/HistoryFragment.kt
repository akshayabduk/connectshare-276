package org.example.app.ui

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.data.HistoryRepository
import java.text.DateFormat
import java.util.*

/**
 * HistoryFragment displays a scrollable list of previous sharing actions.
 */
class HistoryFragment : Fragment() {

    // PUBLIC_INTERFACE
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        /**
         * Inflate history list layout.
         */
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    // PUBLIC_INTERFACE
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /**
         * Bind list and populate from repository.
         */
        super.onViewCreated(view, savedInstanceState)
        val list: RecyclerView = view.findViewById(R.id.recycler_history)
        val empty: TextView = view.findViewById(R.id.empty_history)
        list.layoutManager = LinearLayoutManager(requireContext())

        val repo = HistoryRepository(requireContext())
        val items = repo.getHistory()
        if (items.isEmpty()) {
            empty.visibility = View.VISIBLE
            list.visibility = View.GONE
        } else {
            empty.visibility = View.GONE
            list.visibility = View.VISIBLE
            list.adapter = HistoryAdapter(items)
        }
    }

    private class HistoryAdapter(private val items: List<HistoryRepository.ShareHistoryItem>) :
        RecyclerView.Adapter<HistoryAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val fileName: TextView = v.findViewById(R.id.history_file_name)
            val method: TextView = v.findViewById(R.id.history_method)
            val time: TextView = v.findViewById(R.id.history_time)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.fileName.text = item.fileName
            holder.method.text = item.method
            holder.time.text = DateFormat.getDateTimeInstance().format(Date(item.time))
        }

        override fun getItemCount(): Int = items.size
    }
}
