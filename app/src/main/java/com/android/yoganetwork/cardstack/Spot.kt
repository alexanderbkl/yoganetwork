package com.android.yoganetwork.cardstack

data class Spot(
        val id: Long = counter++,
        val pseudonym: String = "",
        val practic: String = "",
        val type: String = "",
        val image: String = "",
        val description: String = "",
        val uid: String = "",
        val onlineStatus: String = "",
        val isBlocked: Boolean = false,
        val isLiked: Boolean = false

        ) {
    companion object {
        private var counter = 0L
    }

}
