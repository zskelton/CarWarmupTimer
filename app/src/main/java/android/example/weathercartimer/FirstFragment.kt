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
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    // Globals
    private lateinit var txtStatus: TextView
    private lateinit var txtTemp: TextView
    private lateinit var txtTimer: TextView
    private lateinit var txtTimerLength: TextView
    private lateinit var swMomMode: Switch
    private var timerLength: Long = 300
    private var momMode: Boolean = true

    // Convert Kelvin to Fahrenheit
    private fun k2f(inKelvin: Double): Double {
        return (((inKelvin - 273.15) * 9.0)/5.0) + 32.0;
    }

    // Location Function
    // TODO: Fix Location Request.
    private fun createLocationRequest() {
        Log.d("location", "creating location request")

        val builder = LocationSettingsRequest.Builder()

        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity()!!.applicationContext)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            Log.d("location", "received - ${locationSettingsResponse.toString()}")
        }

        task.addOnFailureListener { exception ->
            Log.d("location", "error - ${exception.toString()}")
        }
    }

    private fun startTimer(view: View) {
        txtStatus.setText("Running Timer")
        object : CountDownTimer((timerLength*1000), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                txtTimer.setText("${millisUntilFinished / 1000} secs")
            }

            override fun onFinish() {
                Snackbar.make(view, "Timer Done!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
                txtTimer.setText("DONE!")
                val notification: Uri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(
                    requireActivity()!!.applicationContext,
                    notification
                )
                r.play()
            }
        }.start()
    }

    // Function to Run Web Query
    @SuppressLint("MissingPermission")
    private fun runQuery(view: View, url: String = "https://www.google.com") {
        val TAG = "Query";

        txtStatus.setText("Running Query...")

        var result: String = "";
        // From: https://stackoverflow.com/questions/28599377/how-to-run-volley-in-fragmentnavigation-drawer
        val res = Response.Listener<String> { response ->
            Log.d(TAG, "Got Response: ${response}")
            txtStatus.setText("Query Done.")
            try {
                val coord = JSONObject(response).getJSONObject("coord");
                val lon = coord.getDouble("lon")
                val lat = coord.getDouble("lat")
                val main = JSONObject(response).getJSONObject("main")
                val temp = k2f(main.getDouble("temp"))
                val string = "Temp at $lat, $lon is ${temp.roundToInt()}°F"
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

//      Log.d("location", "getting location")
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location : Location? ->
//                Log.d("location", location.toString())
//            }
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
        txtStatus = view.findViewById(R.id.txt_status)
        txtTemp = view.findViewById(R.id.txt_currentWeather)
        txtTimer = view.findViewById(R.id.txt_timer)
        swMomMode = view.findViewById(R.id.switch_mom)
        txtTimerLength = view.findViewById(R.id.txt_recommendedTime)

        // Start Location
        createLocationRequest()
        // TODO: Fix getting location
        val location = ""

        // Start Timer
        binding.btnStartTimer.setOnClickListener { view ->
            Snackbar.make(view, "Timer Started!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
            startTimer(view)
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
                txtTimer.setText("${timerLength} seconds")
                txtTimerLength.setText("${timerLength} seconds")
                momMode = false
            } else {
                timerLength = 300
                swMomMode.setText("Mom Mode On")
                txtTimer.setText("${timerLength} seconds")
                txtTimerLength.setText("${timerLength} seconds")
                momMode = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}