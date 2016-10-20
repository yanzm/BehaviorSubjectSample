package net.yanzm.behaviorsubjectsample;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * サーバーからデータを取得する部分のモック
 */
public class DataRetriever {

    private static DataRetriever instance;

    public static synchronized DataRetriever getInstance() {
        if (instance == null) {
            instance = new DataRetriever();
        }
        return instance;
    }

    private final Random random = new Random();

    private static final String[] COMMON_DATA_LIST = {
            "Cupcake",
            "Donuts",
            "Eclair",
            "Froyo",
            "Gingerbread",
            "Honeycomb",
            "IceCreamSandwich",
            "JellyBean",
            "Kitkat",
            "Lollipop",
            "Marshmallow",
            "Nougat"
    };

    @NonNull
    public Observable<CommonData> getCommonData() {
        return Observable.just(new CommonData(COMMON_DATA_LIST[random.nextInt(COMMON_DATA_LIST.length)]))
                .delay(2 + random.nextInt(3), TimeUnit.SECONDS);
    }

    @NonNull
    public Observable<SpecificData> getSpecificData(int position) {
        final List<String> data = new ArrayList<>();
        final String prefix = COMMON_DATA_LIST[random.nextInt(COMMON_DATA_LIST.length)];
        for (int i = 0; i < 30; i++) {
            data.add(prefix.toUpperCase() + " : " + position);
        }
        return Observable.just(new SpecificData(data))
                .delay(random.nextInt(3), TimeUnit.SECONDS);
    }
}
