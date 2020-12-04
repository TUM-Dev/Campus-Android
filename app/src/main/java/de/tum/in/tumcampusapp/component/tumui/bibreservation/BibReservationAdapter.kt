package de.tum.`in`.tumcampusapp.component.tumui.bibreservation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.tumui.bibreservation.model.BibAppointment
import org.jetbrains.anko.sdk27.coroutines.onClick

class BibReservationAdapter(private val appointments: ArrayList<BibAppointment>, private val c: Context) : RecyclerView.Adapter<BibReservationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bibDate: TextView = view.findViewById(R.id.date)
        val from: TextView = view.findViewById(R.id.fromTime)
        val til: TextView = view.findViewById(R.id.tilTime)
        val dateWrapper: ConstraintLayout = view.findViewById(R.id.dateWrapper)
        val reserveBtn: Button = view.findViewById(R.id.reserveBtn)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.reservable_bib_entry, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val curElem = appointments[position]
        holder.bibDate.text = curElem.getDay()
        holder.from.text = curElem.getFromTime()
        holder.til.text = curElem.getTilTime()
        holder.reserveBtn.onClick {
            c.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ub.tum.de/reserve/" + appointments[position].reservationId)))
        }

        val curDay = appointments[position].getDay()
        var prevDay = ""
        if (position!=0){
            prevDay = appointments[position - 1].getDay()
        }
        if (position == 0 || curDay != prevDay) {
            holder.dateWrapper.visibility = View.VISIBLE
        }else {
            holder.dateWrapper.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return appointments.size
    }

}