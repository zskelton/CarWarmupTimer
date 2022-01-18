package android.example.weathercartimer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import android.example.weathercartimer.databinding.FragmentFirstBinding
import android.location.Location
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.roundToInt

import android.media.RingtoneManager

import android.media.Ringtone
import android.net.Uri


/**
 * A Fragment to View the Main Activity
 */
class FirstFragment : Fragment() {
    // Globals
    private lateinit var btnStart: Button
    private lateinit var txtStatus: TextView
    private lateinit var txtTemp: TextView
    private lateinit var txtTimer: TextView
    private lateinit var txtTimerLength: TextView
    private lateinit var swMomMode: Switch
    private lateinit var cdTimer: CountDownTimer
    private var timerLength: Long = 300
    private var momMode: Boolean = true
    private var timerOn: Boolean = false
    private var queryRan: Boolean = false

    // Convert Kelvin to Fahrenheit
    private fun k2f(inKelvin: Double): Double {
        return (((inKelvin - 273.15) * 9.0)/5.0) + 32.0;
    }

    private fun startTimer(view: View) {
        txtStatus.setText("Running Timer")
        cdTimer = object : CountDownTimer(timerLength*1000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                txtTimer.setText("${millisUntilFinished / 1000} secs")
            }

            override fun onFinish() {
                txtTimer.setText("DONE!")
                val notification: Uri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(
                    requireActivity()!!.applicationContext,
                    notification)
                r.play()
                Snackbar.make(view, "Timer Done!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        }.start()
    }

    // Function to Run Web Query
    private fun runQuery(view: View, url: String = "https://www.google.com") {
        val TAG = "Query";

        txtStatus.setText("Running Query...")

        var result: String = "";
        // From: https://stackoverflow.com/questions/28599377/how-to-run-volley-in-fragmentnavigation-drawer
        val res = Response.Listener<String> { response ->
            Log.d(TAG, "Got Response: ${response}")
            txtStatus.setText("Query Done.")
            try {
                val main = JSONObject(response).getJSONObject("main")
                val temp = k2f(main.getDouble("temp"))
                val string = "Temp at ${temp.roundToInt()}°F"
                txtTemp.setText("${temp.roundToInt()}°F")
                Log.d("json", string)
            } catch (e: JSONException) {
                Log.e("json error", "JSON Error")
                result = "json error"
                e.printStackTrace()
            }
        }
        val eros = Response.ErrorListener { volleyError -> Snackbar.make(view,"volley error - $volleyError", Snackbar.LENGTH_LONG).setAction("Action", null).show() }
        val request = StringRequest(Request.Method.GET, url, res,eros)
        val rQueue = Volley.newRequestQueue(requireActivity()!!.applicationContext)
        rQueue.add(request)
        queryRan = true
    }

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Interface
        btnStart = view.findViewById(R.id.btn_startTimer)
        txtStatus = view.findViewById(R.id.txt_status)
        txtTemp = view.findViewById(R.id.txt_currentWeather)
        txtTimer = view.findViewById(R.id.txt_timer)
        swMomMode = view.findViewById(R.id.switch_mom)
        txtTimerLength = view.findViewById(R.id.txt_recommendedTime)

        // Start Location
        // TODO: Fix getting location

        // Start Timer or Reset Depending on State
        binding.btnStartTimer.setOnClickListener { view ->
            if(!queryRan) runQuery(view, "https://api.openweathermap.org/data/2.5/weather?appid=8a501082a8bb88ac1a46b416876164b2&q=Ames")
            if(!timerOn)  {
                startTimer(view)
                btnStart.setText("Reset")
                timerOn = true
            } else {
                if(cdTimer != null) cdTimer.cancel()
                txtTimer.setText("$timerLength secs")
                btnStart.setText("Start")
                timerOn = false
            }

        }

        // Get Temperature
        binding.btnRefreshQuery.setOnClickListener { view ->
            // runQuery(view, "https://api.weather.gov/points/42.0486,-93.6945")
            // runQuery(view, "https://ipinfo.io/json?token=99d366e55a0bdb")
            runQuery(view, "https://api.openweathermap.org/data/2.5/weather?appid=8a501082a8bb88ac1a46b416876164b2&q=Ames")
        }

        // Set Timer
        binding.switchMom.setOnClickListener { view ->
            if(momMode) {
                swMomMode.setText("Mom Mode Off")
                timerLength = 10
                txtTimer.setText("${timerLength} secs")
                txtTimerLength.setText("${timerLength} secs")
                momMode = false
            } else {
                timerLength = 300
                swMomMode.setText("Mom Mode On")
                txtTimer.setText("${timerLength} secs")
                txtTimerLength.setText("${timerLength} secs")
                momMode = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}