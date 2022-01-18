package android.example.weathercartimer

import android.example.weathercartimer.databinding.FragmentFirstBinding
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.roundToInt


/**
 * A Fragment to View the Main Activity
 */
class FirstFragment : Fragment() {
    /* GLOBALS */
    private lateinit var btnStart: Button
    private lateinit var txtStatus: TextView
    private lateinit var txtTemp: TextView
    private lateinit var txtTimer: TextView
    private lateinit var txtTimerLength: TextView
    private lateinit var swMomMode: SwitchMaterial
    private lateinit var cdTimer: CountDownTimer
    private lateinit var pBarTime: ProgressBar
    private var timerLength: Long = 300
    private var momMode: Boolean = true
    private var timerOn: Boolean = false
    private var queryRan: Boolean = false

    /* BEGIN UTILITY FUNCTIONS */
    // Convert Kelvin to Fahrenheit
    private fun k2f(inKelvin: Double): Double {
        // Return Conversion
        return (((inKelvin - 273.15) * 9.0)/5.0) + 32.0
    }

    // Function to Start Timer
    private fun startTimer(view: View) {
        // Variables
        val tag = "timer"
        pBarTime.progress = 100

        // Announce
        txtStatus.text = getString(R.string.txt_running_timer)
        Log.i(tag, "Begin Timer Function")

        // Load Timer into Global
        cdTimer = object : CountDownTimer(timerLength*1000, 1000) {
            // Run on Tick
            override fun onTick(millisUntilFinished: Long) {
                // Update Timer
                txtTimer.text = getString(R.string.txt_current_time, (millisUntilFinished / 1000))
                // Update Progress Bar
                val progress = (millisUntilFinished/timerLength)/10
                pBarTime.progress = progress.toInt()
            }

            // Run on Finish
            override fun onFinish() {
                // Update Text
                txtTimer.text = getString(R.string.txt_done)
                // Play Ringtone
                val notification: Uri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(
                    requireActivity().applicationContext,
                    notification)
                r.play()
                // Announce Success
                Snackbar.make(view, "Timer Done!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }.start()

        // Announce
        Log.i(tag, "End Timer Function")
    }

    // Function to Run Web Query
    private fun runQuery(view: View) {
        // Variables
        val tag = "Query"
        val url = "https://api.openweathermap.org/data/2.5/weather?appid=8a501082a8bb88ac1a46b416876164b2&q=Ames"
        queryRan = false

        // Announce
        txtStatus.text = getString(R.string.txt_query_running)
        Log.i(tag, "Begin Query Function")

        // Set Response Listener
        val res = Response.Listener<String> { response ->
            Log.d(tag, "Got Response: $response")
            txtStatus.text = getString(R.string.txt_query_done)
            try {
                // Parse JSON
                val main = JSONObject(response).getJSONObject("main")
                val temp = k2f(main.getDouble("temp"))
                // Adjust Time to Temperature
                val tempInt = temp.roundToInt()
                txtTemp.text = getString(R.string.txt_weather_current, tempInt)
                Log.d("json", main.toString())
                timerLength = when {
                    tempInt > 32 -> 10
                    tempInt in 21..32 -> 100
                    tempInt in 11..20 -> 200
                    tempInt < 10 -> 300
                    else -> 300
                }
                // Update Labels
                txtTimer.text = getString(R.string.txt_current_time, timerLength)
                txtTimerLength.text = getString(R.string.txt_current_time, timerLength)
            } catch (e: JSONException) {
                // Error in JSON
                Log.e("json", "JSON Error")
                e.printStackTrace()
            }
        }

        // Set Error Handler
        val eros = Response.ErrorListener { volleyError ->
            Log.e(tag, volleyError.toString())
            Snackbar.make(view,"volley error - $volleyError", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }

        // Create Request
        val request = StringRequest(Request.Method.GET, url, res,eros)

        // Add to Queue
        val rQueue = Volley.newRequestQueue(requireActivity().applicationContext)
        rQueue.add(request)

        // Announce Complete
        Log.i(tag, "End Query Function")
        queryRan = true
    }
    /* END UTILITY FUNCTIONS */

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // Create View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Run on Start
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Interface
        btnStart = view.findViewById(R.id.btn_startTimer)
        txtStatus = view.findViewById(R.id.txt_status)
        txtTemp = view.findViewById(R.id.txt_currentWeather)
        txtTimer = view.findViewById(R.id.txt_timer)
        swMomMode = view.findViewById(R.id.switch_mom)
        txtTimerLength = view.findViewById(R.id.txt_recommendedTime)
        pBarTime = view.findViewById(R.id.bar_timerProgress)

        // Initialize Labels
        swMomMode.text = getString(R.string.switch_mom, "On")
        txtTimer.text = getString(R.string.txt_current_time, timerLength)
        txtTimerLength.text = getString(R.string.txt_current_time, timerLength)

        // Run Query Early
        runQuery(view)

        // Start Location
        // TODO: Fix getting location

        // Start Timer or Reset Depending on State
        binding.btnStartTimer.setOnClickListener { view_ ->
            if(!queryRan) runQuery(view_)
            if(!timerOn)  {
                startTimer(view_)
                btnStart.text = getString(R.string.txt_reset)
                timerOn = true
            } else {
                cdTimer.cancel()
                pBarTime.progress = 45
                txtTimer.text = getString(R.string.txt_current_time, timerLength)
                btnStart.text = getString(R.string.txt_start)
                timerOn = false
            }
        }

        // Get Temperature
        binding.btnRefreshQuery.setOnClickListener { view_ ->
            runQuery(view_)
        }

        // Set Timer
        binding.switchMom.setOnClickListener {
            // Kill Active Timer
            if(timerOn) {
                btnStart.text = getString(R.string.txt_start)
                cdTimer.cancel()
                pBarTime.progress = 45
                timerOn = false
            }
            // Set New Times
            if(momMode) {
                timerLength = 10
                swMomMode.text = getString(R.string.switch_mom, "Off")
                momMode = false
            } else {
                timerLength = 300
                swMomMode.text = getString(R.string.switch_mom, "On")
                momMode = true
            }
            // Update Labels
            txtTimer.text = getString(R.string.txt_current_time, timerLength)
            txtTimerLength.text = getString(R.string.txt_current_time, timerLength)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}