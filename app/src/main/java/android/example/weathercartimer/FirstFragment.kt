package android.example.weathercartimer

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import android.example.weathercartimer.databinding.FragmentFirstBinding
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.android.volley.RequestQueue
import com.android.volley.VolleyError


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    // Function to Run Web Query
    private fun runQuery(view: View) {
        val TAG = "Query";
        val textView = view.findViewById<TextView>(R.id.txt_status)
        // From: https://stackoverflow.com/questions/28599377/how-to-run-volley-in-fragmentnavigation-drawer
        val url = "https://www.google.com"
        val res = Response.Listener<String> { response -> Snackbar.make(view, "success: ${response}", Snackbar.LENGTH_LONG).setAction("Action", null).show(); Log.d(TAG, response) }
        val eros = Response.ErrorListener { volleyError -> Snackbar.make(view,"volley error - $volleyError", Snackbar.LENGTH_LONG).setAction("Action", null).show() }
        val request = StringRequest(Request.Method.GET, url, res,eros)
        val rQueue = Volley.newRequestQueue(requireActivity()!!.applicationContext)
        rQueue.add(request)
    }

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStartTimer.setOnClickListener { view ->
            runQuery(view)
            Snackbar.make(view, "Done", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}