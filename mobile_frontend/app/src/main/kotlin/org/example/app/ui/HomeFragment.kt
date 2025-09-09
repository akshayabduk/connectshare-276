package org.example.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.example.app.R
import org.example.app.utils.SuggestionEngine

/**
 * HomeFragment displays AI-driven sharing suggestions based on user history
 * and highlights a prominent AI assistant banner with quick actions.
 */
class HomeFragment : Fragment() {

    // PUBLIC_INTERFACE
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * Inflate the suggestions list view along with an AI assistant banner.
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
         * Bind RecyclerView, AI banner UI, and load suggestions.
         *
         * Parameters:
         * - view: Root view
         * - savedInstanceState: State
         *
         * Returns: None
         */
        super.onViewCreated(view, savedInstanceState)

        // AI banner UI
        val chips: ChipGroup = view.findViewById(R.id.chip_actions)
        val aiProgress: View = view.findViewById(R.id.ai_progress)
        val aiStatusText: TextView = view.findViewById(R.id.ai_status_text)

        // Simulate AI loading then populate quick actions
        aiProgress.visibility = View.VISIBLE
        aiStatusText.visibility = View.VISIBLE
        view.postDelayed({
            aiProgress.visibility = View.GONE
            aiStatusText.visibility = View.GONE

            chips.removeAllViews()
            addActionChip(chips, getString(R.string.ai_suggested)) {
                Toast.makeText(requireContext(), getString(R.string.ai_suggested), Toast.LENGTH_SHORT).show()
            }
            addActionChip(chips, getString(R.string.select_files)) {
                Toast.makeText(requireContext(), getString(R.string.select_files), Toast.LENGTH_SHORT).show()
            }
            addActionChip(chips, getString(R.string.scan_title)) {
                Toast.makeText(requireContext(), getString(R.string.scan_title), Toast.LENGTH_SHORT).show()
            }
        }, 600)

        // Suggestions feed
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

    private fun addActionChip(group: ChipGroup, text: String, onClick: () -> Unit) {
        val chip = Chip(requireContext()).apply {
            this.text = text
            isClickable = true
            isCheckable = false
            setOnClickListener { onClick.invoke() }
        }
        group.addView(chip)
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
