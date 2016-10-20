package net.yanzm.behaviorsubjectsample;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 各タブで共通のデータ
 */
public class CommonData {

    @NonNull
    public static CommonData empty() {
        return new CommonData(null);
    }

    @Nullable
    private final String data;

    public CommonData(@Nullable String data) {
        this.data = data;
    }

    public boolean isEmpty() {
        return data == null;
    }

    @Nullable
    public String getData() {
        return data;
    }
}
