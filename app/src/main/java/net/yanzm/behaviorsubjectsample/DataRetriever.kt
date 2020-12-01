package net.yanzm.behaviorsubjectsample

import rx.Observable
import java.util.ArrayList
import java.util.Locale
import java.util.Random
import java.util.concurrent.TimeUnit

/**
 * サーバーからデータを取得する部分のモック
 */
object DataRetriever {

    private val random = Random()

    private fun delayTime(offset: Int = 0): Long {
        return (offset + random.nextInt(1)).toLong()
    }

    fun getCommonData(): Observable<CommonData> {
        return Observable.just(CommonData(COMMON_DATA_LIST.random()))
            .delay(delayTime(1), TimeUnit.SECONDS)
    }

    fun getSpecificData(position: Int): Observable<SpecificData> {
        val prefix = COMMON_DATA_LIST.random().toUpperCase(Locale.ENGLISH)
        val data = (0..29).map {
            "$prefix : $position"
        }
        return Observable.just(SpecificData(data))
            .delay(delayTime(), TimeUnit.SECONDS)
    }

    private val COMMON_DATA_LIST = arrayOf(
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
    )
}
