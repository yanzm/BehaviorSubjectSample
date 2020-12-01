package net.yanzm.behaviorsubjectsample

/**
 * 各タブで共通のデータ
 */
class CommonData(val data: String?) {

    val isEmpty: Boolean
        get() = data == null

    companion object {
        fun empty(): CommonData = CommonData(null)
    }
}

/**
 * タブ固有のデータ
 */
data class SpecificData(val data: List<String>)
