import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bqliang.leavesheet.databinding.FragmentAnnexBinding

class AnnexFragment : Fragment() {

    private lateinit var binding: FragmentAnnexBinding
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnnexBinding.inflate(inflater, container, false)
        pickMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
                val fileNames = mutableListOf<String>()
                uris?.forEach { uri ->
                    val cursor =
                        requireContext().contentResolver.query(uri, null, null, null, null, null)
                    cursor?.moveToFirst()
                    cursor?.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                        ?.let {
                            fileNames.add(it)
                        }
                    cursor?.close()
                }
            }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fab.setOnClickListener {
            val pickVisualMediaRequest =
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            Toast.makeText(requireContext(), "请选择附件", Toast.LENGTH_SHORT).show()
            pickMedia.launch(pickVisualMediaRequest)
        }
    }
}