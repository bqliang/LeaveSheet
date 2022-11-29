package com.bqliang.leavesheet.settings

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.leavesheet.adapter.AnnexAdapter
import com.bqliang.leavesheet.collectLifecycleFlow
import com.bqliang.leavesheet.data.database.LeaveSheetDatabase
import com.bqliang.leavesheet.data.database.dao.AnnexDao.Companion.ANNEXES_MAX_LIMIT
import com.bqliang.leavesheet.data.database.entity.Annex
import com.bqliang.leavesheet.data.repository.Repository
import com.bqliang.leavesheet.databinding.FragmentAnnexBinding
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class AnnexFragment : Fragment() {

    private lateinit var pickMultipleImage: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var binding: FragmentAnnexBinding
    private val annexAdapter by lazy { AnnexAdapter() }
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private val annexes: Flow<List<Annex>> by lazy {
        LeaveSheetDatabase.getDatabase().annexDao().loadAllAnnexDesc()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickMultipleImage =
            registerForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(
                    ANNEXES_MAX_LIMIT /* max limit */
                )
            ) { uris ->
                val annexList = mutableListOf<Annex>()

                uris.takeLast(ANNEXES_MAX_LIMIT).forEach { uri ->
                    val fileName = getFileNameFromUri(uri)
                    val insertTime = System.currentTimeMillis()
                    val annex = Annex(fileName, insertTime)
                    annexList.add(annex)
                }
                lifecycleScope.launch { Repository.insertAnnexes(annexList) }
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnnexBinding.inflate(inflater, container, false)
        binding.annexRecyclerView.adapter = annexAdapter
        // 设置分割线
        val materialDividerItemDecoration = MaterialDividerItemDecoration(
            requireContext(),
            LinearLayoutManager.VERTICAL
        )
        binding.annexRecyclerView.addItemDecoration(materialDividerItemDecoration)

        // 向下滑动时收起 FAB，向上滑动时展开 FAB
        binding.annexRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && binding.fab.isExtended) binding.fab.shrink()
                else if (dy < 0 && !binding.fab.isExtended) binding.fab.extend()
            }
        })

        SwipeToDeleteCallback { absoluteAdapterPosition ->
            lifecycleScope.launch {
                Repository.deleteAnnex(annexAdapter.currentList[absoluteAdapterPosition])
            }
        }.let { swipeToDeleteCallback ->
            ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(binding.annexRecyclerView)
        }

        collectLifecycleFlow(annexes) { annexList ->
            annexAdapter.submitList(annexList)
            if (annexList.isEmpty()) {
                binding.annexRecyclerView.fadeOut()
                binding.emptyIcon.fadeIn()
                binding.emptyText.fadeIn()
            } else {
                binding.emptyView.visibility = View.GONE
                binding.annexRecyclerView.alpha = 1f
                binding.annexRecyclerView.visibility = View.VISIBLE
            }
        }

        binding.notShowTipsBtn.setOnClickListener {
            mmkv.putBoolean("not_show_tips", true)
            binding.tips.visibility = View.GONE
        }

        binding.tips.visibility =
            if (mmkv.getBoolean("not_show_tips", false)) View.GONE else View.VISIBLE

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = Bundle().apply {
            putSerializable(
                SettingsActivity.TOOLBAR_TYPE,
                SettingsActivity.Toolbar.ANNEX
            )
        }
        setFragmentResult(SettingsActivity.TOOLBAR_TYPE, bundle)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        binding.fab.setOnClickListener {
            val pickVisualMediaRequest =
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            pickMultipleImage.launch(pickVisualMediaRequest)
            Toast.makeText(requireContext(), "请选择附件 (可多选)", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getFileNameFromUri(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null, null)
        cursor?.moveToFirst()
        val fileName = cursor?.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        cursor?.close()
        return fileName ?: ""
    }


    private inline fun <reified T : View> T.crossfade(out: Boolean) = lifecycleScope.launch {
        val targetVisibility = if (out) View.GONE else View.VISIBLE
        val targetAlpha = if (out) 0f else 1f
        val listen = if (out) object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.GONE
            }
        } else null

        if (visibility != targetVisibility) {
            if (!out) {
                alpha = 0f
                visibility = View.VISIBLE
            }

            animate()
                .alpha(targetAlpha)
                .setDuration(300)
                .setListener(listen)
                .start()

        }
    }

    private inline fun <reified T : View> T.fadeIn() = this.crossfade(false)

    private inline fun <reified T : View> T.fadeOut() = this.crossfade(true)
}