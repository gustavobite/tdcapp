package br.com.nglauber.tdcapp.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import br.com.nglauber.tdcapp.R
import br.com.nglauber.tdcapp.presentation.AppViewModelFactory
import br.com.nglauber.tdcapp.presentation.ModalityListViewModel
import br.com.nglauber.tdcapp.presentation.ViewState
import br.com.nglauber.tdcapp.repository.model.Modality
import br.com.nglauber.tdcapp.ui.adapter.ModalitiesPagerAdapter
import kotlinx.android.synthetic.main.activity_modality_list.*

class ModalityListActivity : AppCompatActivity() {

    //TODO inject
    private val viewModel: ModalityListViewModel by lazy {
        val factory = AppViewModelFactory(this.application)
        ViewModelProviders.of(this, factory).get(ModalityListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modality_list)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val eventId = intent.getIntExtra(EXTRA_EVENT_ID, -1)
        if (eventId == -1) {
            finish()
            return
        }
        fetchActivities(eventId)
    }

    private fun fetchActivities(eventId: Int) {
        viewModel.getState().observe(this, Observer { newState ->
            newState?.let {
                handleState(eventId, it)
            }
        })
        if (viewModel.getState().value == null) {
            viewModel.fetchModalities(eventId)
        }
    }

    private fun handleState(eventId: Int, state: ViewState<Map<String, List<Modality>>>?) {
        when (state?.status) {
            ViewState.Status.LOADING -> {
                progressBar.visibility = View.VISIBLE
            }
            ViewState.Status.SUCCESS -> {
                state.data?.let {
                    handleSuccess(eventId, it.keys.toList())
                }
            }
            ViewState.Status.ERROR -> {
                state.error?.let {
                    handleError(it)
                }
            }
        }
    }

    private fun handleSuccess(eventId: Int, modalityDates: List<String>) {
        progressBar.visibility = View.GONE
        viewPager.adapter = ModalitiesPagerAdapter(
                supportFragmentManager, eventId, modalityDates
        )
        tabs.setupWithViewPager(viewPager)
    }

    private fun handleError(e: Throwable) {
        e.printStackTrace()
        progressBar.visibility = View.GONE
        Toast.makeText(this, R.string.error_loading_activities, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_EVENT_ID = "eventId"

        fun startActivity(context: Context, eventId: Int) {
            context.startActivity(Intent(context, ModalityListActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
            })
        }
    }
}