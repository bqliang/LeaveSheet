package com.bqliang.leavesheet.settings

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.MenuRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bqliang.leavesheet.BaseActivity
import com.bqliang.leavesheet.R
import com.bqliang.leavesheet.databinding.ActivitySettingsBinding
import com.google.android.material.transition.MaterialSharedAxis

class SettingsActivity : BaseActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {


    enum class Toolbar(val title: String, @MenuRes val menuResId: Int? = null) {
        SETTINGS("设置", R.menu.settings_menu),
        ANNEX("附件", null)
    }

    private lateinit var binding: ActivitySettingsBinding

    companion object {
        const val TOOLBAR_TYPE = "toolbar_type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivitySettingsBinding.inflate(layoutInflater)

        supportFragmentManager.setFragmentResultListener(
            TOOLBAR_TYPE,
            this
        ) { requestKey, bundle ->
            val toolbar = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(TOOLBAR_TYPE, Toolbar::class.java)
            } else {
                @Suppress("deprecation")
                bundle.getSerializable(TOOLBAR_TYPE) as? Toolbar
            }

            toolbar?.let {
                binding.toolbar.title = it.title
                binding.toolbar.menu.clear()
                it.menuResId?.let { menuResId ->
                    binding.toolbar.inflateMenu(menuResId)
                }
            }
        }

        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_about -> binding.fragmentContainer.getFragment<SettingsFragment>()
                    .showAboutDialog()
            }
            true
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainer) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
    }


    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment!!
        )
        fragment.arguments = args
        fragment.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        fragment.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        caller.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        caller.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        return true
    }
}
