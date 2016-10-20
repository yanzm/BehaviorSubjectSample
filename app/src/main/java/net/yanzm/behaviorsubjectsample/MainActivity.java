package net.yanzm.behaviorsubjectsample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.Subscriptions;

public class MainActivity extends AppCompatActivity implements MainFragment.MainFragmentListener {

    private final BehaviorSubject<CommonData> commonDataBehaviorSubject = BehaviorSubject.create();

    @NonNull
    private Subscription subscription = Subscriptions.empty();

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.pager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        refreshCommonData();
    }

    @Override
    protected void onDestroy() {
        commonDataBehaviorSubject.onCompleted();
        subscription.unsubscribe();
        super.onDestroy();
    }

    private void refresh() {
        refreshCommonData();

        // 現在インスタンス化されている MainFragment に取り直しを命じる
        final List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof MainFragment) {
                    ((MainFragment) fragment).refresh();
                }
            }
        }
    }

    private void refreshCommonData() {
        // 取り直しなのでここで null をセットして新しく作られたタブで前の値が流れないようにする
        commonDataBehaviorSubject.onNext(null);

        subscription = DataRetriever.getInstance().getCommonData()
                .onErrorReturn(new Func1<Throwable, CommonData>() {
                    @Override
                    public CommonData call(Throwable throwable) {
                        // エラーのときはデータがないものとして扱う
                        return CommonData.empty();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CommonData>() {
                    @Override
                    public void call(CommonData commonData) {
                        swipeRefreshLayout.setRefreshing(false);
                        commonDataBehaviorSubject.onNext(commonData);
                    }
                });
    }

    @NonNull
    @Override
    public Observable<CommonData> getCommonDataObservable() {
        return commonDataBehaviorSubject
                .filter(new Func1<CommonData, Boolean>() {
                    @Override
                    public Boolean call(CommonData commonData) {
                        // 取り直しのときに以前の値を流さないため
                        return commonData != null;
                    }
                });
    }

    static class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return MainFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "TAB" + position;
        }
    }

}
