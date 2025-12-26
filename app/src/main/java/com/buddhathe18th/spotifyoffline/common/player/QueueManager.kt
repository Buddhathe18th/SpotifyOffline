package com.buddhathe18th.spotifyoffline.common.player

// Single shared queue instance for the whole app process
object QueueManager {
    val playQueue: PlayQueue = PlayQueue()
}
