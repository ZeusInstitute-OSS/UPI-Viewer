import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zeusinstitute.upiapp.R

class AboutAppFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_about_app, container, false)

        view.findViewById<Button>(R.id.sourceCodeButton).setOnClickListener {
            openUrl("https://github.com/zeusinstitute-oss/upi-viewer")
        }

        view.findViewById<Button>(R.id.licenseButton).setOnClickListener {
            openUrl("https://github.com/zeusinstitute-oss/upi-viewer?tab=GPL-3.0-1-ov-file#readme")
        }

        view.findViewById<Button>(R.id.authorButton).setOnClickListener {
            openUrl("https://sounddrill31.github.io")
        }

        return view
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No application can handle this request. Please install a web browser.", Toast.LENGTH_LONG).show()
        }
    }
}