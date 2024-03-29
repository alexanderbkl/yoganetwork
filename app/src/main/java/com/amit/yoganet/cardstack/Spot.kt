package com.amit.yoganet.cardstack

data class Spot(
    val id: Long = counter++,
    val pseudonym: String = "",
    val practic: String = "",
    val purpose: String = "",
    val country: String = "",
    val city: String = "",
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
