package net.yanzm.behaviorsubjectsample;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * タブ固有のデータ
 */
public class SpecificData {

    @NonNull
    private final List<String> data;

    public SpecificData(@NonNull List<String> data) {
        this.data = data;
    }

    @NonNull
    public List<String> getData() {
        return data;
    }
}
