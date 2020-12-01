package net.yanzm.behaviorsubjectsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import net.yanzm.behaviorsubjectsample.CommonData.Companion.empty
import net.yanzm.behaviorsubjectsample.DataRetriever.getCommonData
import net.yanzm.behaviorsubjectsample.databinding.ActivityMainBinding
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subscriptions.Subscriptions

class MainActivity : AppCompatActivity(), MainFragmentListener {

    private val commonDataBehaviorSubject = BehaviorSubject.create<CommonData?>()
    private var subscription = Subscriptions.empty()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.swipeRefreshLayout.setOnRefreshListener { refresh() }
        binding.viewPager.adapter = MyPagerAdapter(supportFragmentManager)

        refreshCommonData()
    }

    override fun onDestroy() {
        commonDataBehaviorSubject.onCompleted()
        subscription.unsubscribe()
        super.onDestroy()
    }

    private fun refresh() {
        refreshCommonData()

        // 現在インスタンス化されている MainFragment に取り直しを命じる
        for (fragment in supportFragmentManager.fragments) {
            if (fragment is MainFragment) {
                fragment.refresh()
            }
        }
    }

    private fun refreshCommonData() {
        // 取り直しなのでここで null をセットして新しく作られたタブで前の値が流れないようにする
        commonDataBehaviorSubject.onNext(null)

        subscription = getCommonData()
            .onErrorReturn { empty() }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.swipeRefreshLayout.isRefreshing = false
                commonDataBehaviorSubject.onNext(it)
            }
    }

    override fun getCommonDataObservable(): Observable<CommonData> {
        return commonDataBehaviorSubject.filter { it != null }
    }
}

private class MyPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = MainFragment.newInstance(position)

    override fun getCount(): Int = 10

    override fun getPageTitle(position: Int): CharSequence = "TAB$position"
}
