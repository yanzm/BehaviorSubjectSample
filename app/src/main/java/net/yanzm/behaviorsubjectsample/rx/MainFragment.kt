package net.yanzm.behaviorsubjectsample

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import net.yanzm.behaviorsubjectsample.CommonData.Companion.empty
import net.yanzm.behaviorsubjectsample.databinding.FragmentMainBinding
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.Subscriptions

interface MainFragmentListener {
    fun getCommonDataObservable(): Observable<CommonData>
}

class MainFragment : Fragment() {

    private var listener: MainFragmentListener? = null

    private var subscription = Subscriptions.empty()
    private val adapter = StringAdapter()

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? MainFragmentListener
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.recyclerView.adapter = adapter

        if (savedInstanceState == null) {
            refresh()
        }

        return binding.root
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        subscription.unsubscribe()
        super.onDestroy()
    }

    fun refresh() {
        // 共通データとタブ独自のデータ両方揃うまで待ち合わせ
        subscription = Observable
            .combineLatest(
                commonDataObservable,
                specificDataObservable
            ) { commonData, specificData -> commonData to specificData }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (commonData, specificData) ->
                val list = mutableListOf<String>().apply {
                    add("CommonData : " + if (commonData.isEmpty) "empty" else commonData.data)
                    addAll(specificData.data)
                }
                adapter.submitList(list)
            }
    }

    /**
     * 共通のデータを取得
     */
    private val commonDataObservable: Observable<CommonData>
        get() = listener?.getCommonDataObservable() ?: Observable.just(empty())

    /**
     * このタブ独自のデータを取得
     */
    private val specificDataObservable: Observable<SpecificData>
        get() {
            val position = arguments?.getInt(ARGS_POSITION, -1) ?: -1
            return DataRetriever.getSpecificData(position)
        }

    companion object {
        private const val ARGS_POSITION = "position"

        fun newInstance(position: Int): MainFragment {
            return MainFragment().apply {
                arguments = bundleOf(ARGS_POSITION to position)
            }
        }
    }
}
