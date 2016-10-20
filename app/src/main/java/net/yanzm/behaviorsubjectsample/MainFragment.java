package net.yanzm.behaviorsubjectsample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainFragment extends Fragment {

    private static final String ARGS_POSITION = "position";

    @NonNull
    public static MainFragment newInstance(int position) {
        final MainFragment f = new MainFragment();
        final Bundle args = new Bundle();
        args.putInt(ARGS_POSITION, position);
        f.setArguments(args);
        return f;
    }

    public interface MainFragmentListener {

        @NonNull
        Observable<CommonData> getCommonDataObservable();
    }

    @Nullable
    private MainFragmentListener listener;

    @NonNull
    private Subscription subscription = Subscriptions.empty();

    private DataAdapter adapter;
    private Unbinder unbinder;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.progress)
    View progressView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainFragmentListener) {
            listener = (MainFragmentListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        recyclerView.setAdapter(null);
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (adapter == null) {
            adapter = new DataAdapter();
            refresh();
        }
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }

    void refresh() {
        if (getView() != null) {
            recyclerView.setVisibility(View.GONE);
            progressView.setVisibility(View.VISIBLE);
        }

        adapter.clear();

        // 共通データとタブ独自のデータ両方揃うまで待ち合わせ
        subscription = Observable
                .combineLatest(
                        getCommonDataObservable(),
                        getSpecificDataObservable(),
                        new Func2<CommonData, SpecificData, Pair<CommonData, SpecificData>>() {
                            @Override
                            public Pair<CommonData, SpecificData> call(CommonData commonData, SpecificData specificData) {
                                return new Pair<>(commonData, specificData);
                            }
                        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<CommonData, SpecificData>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (getView() != null) {
                            recyclerView.setVisibility(View.VISIBLE);
                            progressView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNext(Pair<CommonData, SpecificData> combinedData) {
                        if (getView() != null) {
                            recyclerView.setVisibility(View.VISIBLE);
                            progressView.setVisibility(View.GONE);
                        }

                        final List<String> list = new ArrayList<>();

                        final CommonData commonData = combinedData.first;
                        list.add("CommonData : " + (commonData.isEmpty() ? "empty" : commonData.getData()));

                        final SpecificData specificData = combinedData.second;
                        list.addAll(specificData.getData());

                        adapter.addAll(list);
                    }
                });
    }

    /**
     * 共通のデータを取得
     */
    private Observable<CommonData> getCommonDataObservable() {
        return listener != null
                ? listener.getCommonDataObservable().first()
                : Observable.just(CommonData.empty());
    }

    /**
     * このタブ独自のデータを取得
     */
    private Observable<SpecificData> getSpecificDataObservable() {
        final int position = getArguments() == null ? -1 : getArguments().getInt(ARGS_POSITION);
        return DataRetriever.getInstance().getSpecificData(position);
    }

    public static class DataAdapter extends RecyclerView.Adapter<DataViewHolder> {

        @NonNull
        private final List<String> data = new ArrayList<>();
        private final Object lock = new Object();

        @Override
        public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View view = inflater.inflate(R.layout.list_item, parent, false);
            return new DataViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DataViewHolder holder, int position) {
            holder.textView.setText(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void clear() {
            synchronized (lock) {
                int itemCount = data.size();
                data.clear();
                notifyItemRangeRemoved(0, itemCount);
            }
        }

        public void addAll(@NonNull Collection<? extends String> collection) {
            synchronized (lock) {
                int itemCount = collection.size();
                int startPosition = data.size();
                data.addAll(collection);
                notifyItemRangeInserted(startPosition, itemCount);
            }
        }
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {

        final TextView textView;

        DataViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }

}
