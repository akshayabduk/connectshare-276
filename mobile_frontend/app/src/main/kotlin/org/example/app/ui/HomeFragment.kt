package org.example.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.utils.SuggestionEngine

/**
 * HomeFragment displays AI-driven sharing suggestions based on user history.
 */
class HomeFragment : Fragment() {

    // PUBLIC_INTERFACE
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * Inflate the suggestions list view.
         *
         * Parameters:
         * - inflater: LayoutInflater
         * - container: Optional container
         * - savedInstanceState: State
         *
         * Returns: Root view for the fragment.
         */
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    // PUBLIC_INTERFACE
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /**
         * Bind RecyclerView and load suggestions.
         *
         * Parameters:
         * - view: Root view
         * - savedInstanceState: State
         *
         * Returns: None
         */
        super.onViewCreated(view, savedInstanceState)
        val list: RecyclerView = view.findViewById(R.id.recycler_suggestions)
        val empty: TextView = view.findViewById(R.id.empty_suggestions)
        list.layoutManager = LinearLayoutManager(requireContext())

        val suggestions = SuggestionEngine.getSuggestions(requireContext())
        if (suggestions.isEmpty()) {
            empty.visibility = View.VISIBLE
            list.visibility = View.GONE
        } else {
            empty.visibility = View.GONE
            list.visibility = View.VISIBLE
            list.adapter = SuggestionAdapter(suggestions)
        }
    }

    /**
     * Adapter for simple suggestion strings.
     */
    private class SuggestionAdapter(
        private val items: List<String>
    ) : RecyclerView.Adapter<SuggestionAdapter.VH>() {

        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val text: TextView = itemView.findViewById(R.id.suggestion_text)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_suggestion, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.text.text = items[position]
        }

        override fun getItemCount(): Int = items.size
    }
}
