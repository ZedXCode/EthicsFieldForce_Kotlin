package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ethicstechno.com.fieldforce.databinding.ItemAttendanceBinding
import ethicstechno.com.fieldforce.models.attendance.CurrentMonthAttendanceResponse
import ethicstechno.com.fieldforce.models.reports.TripSummeryReportResponse

class AttendanceAdapter(
    private val attendanceList: ArrayList<CurrentMonthAttendanceResponse>?,
    private val tripSummeryReportList: ArrayList<TripSummeryReportResponse>?,
    private val isFromAttendanceReport: Boolean,
    private val isFromTripSummery: Boolean,
    clickEvent: LocationClick
) : RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    var locationClick: LocationClick = clickEvent


    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAttendanceBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (isFromTripSummery) tripSummeryReportList!!.size else attendanceList!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attendanceData =
            if (isFromTripSummery) CurrentMonthAttendanceResponse() else attendanceList!![position]
        val tripSummeryData =
            if (isFromTripSummery) tripSummeryReportList!![position] else TripSummeryReportResponse()
        holder.bind(attendanceData, tripSummeryData)
    }

    inner class ViewHolder(private val binding: ItemAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            attendanceData: CurrentMonthAttendanceResponse,
            tripSummeryData: TripSummeryReportResponse
        ) {
            /*if(isFromTripSummery || isFromAttendanceReport){
                binding.tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F)
            }*/
            if (isFromTripSummery) {
                binding.llAttendance.visibility = View.GONE
                binding.llTripSummery.visibility = View.VISIBLE
                binding.tvDateTrip.text = tripSummeryData.tripDate
                binding.tvTypeTrip.text = tripSummeryData.attendanceStatus
                binding.tvVisitCountTrip.text = tripSummeryData.totalTrips.toString()
                binding.tvMapKmTrip.text = tripSummeryData.mapKm.toString()
                binding.tvActualKmTrip.text = tripSummeryData.actualKm.toString()
                binding.imgMapTrip.setOnClickListener {
                    locationClick.onLocationClick(
                        attendanceData,
                        tripSummeryData
                    )
                }
            } else {
                if (isFromAttendanceReport) {
                    binding.llAttendanceReport.visibility = View.VISIBLE
                    binding.llAttendance.visibility = View.GONE
                    binding.tvDateReport.text = attendanceData.punchInDate
                    binding.tvInTimeReport.text = attendanceData.punchInTime
                    binding.tvOutTimeReport.text = attendanceData.punchOutTime
                    binding.tvTypeReport.text = attendanceData.attendanceStatus
                    binding.imgMapReport.setOnClickListener {
                        locationClick.onLocationClick(
                            attendanceData,
                            tripSummeryData
                        )
                    }
                } else {
                    binding.llAttendance.visibility = View.VISIBLE
                    binding.llAttendanceReport.visibility = View.GONE
                    binding.tvDate.text = attendanceData.punchInDate
                    binding.tvInTime.text = attendanceData.punchInTime
                    binding.tvOutTime.text = attendanceData.punchOutTime
                    binding.tvType.text = attendanceData.attendanceStatus
                }

            }

            if (binding.tvInTime.text.toString().isEmpty()) {
                binding.tvInTime.text = "-"
            }
            if (binding.tvOutTime.text.toString().isEmpty()) {
                binding.tvOutTime.text = "-"
            }
            if(binding.tvInTimeReport.text.toString().isEmpty()){
                binding.tvInTimeReport.text = "-"
            }
            if(binding.tvOutTimeReport.text.toString().isEmpty()){
                binding.tvOutTimeReport.text = "-"
            }

            binding.executePendingBindings()
        }
    }

    interface LocationClick {
        fun onLocationClick(
            any: CurrentMonthAttendanceResponse,
            tripSummeryData: TripSummeryReportResponse
        )
    }

}