package com.android.yoganetwork.cardstack

data class Spot(
    val id: Long = counter++,
    val pseudonym: String = "",
    val practic: String = "",
    val type: String = "",
    val imageFull: String = "",
    val description: String = "",
    var uid: String = "",
    val onlineStatus: String = "",
    val isBlocked: Boolean = false,
    var isLiked: Boolean = false

        ) {
    companion object {
        private var counter = 0L
    }

}
