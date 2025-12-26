package com.buddhathe18th.spotifyoffline


/**
 * Manages the current play session with queue operations, shuffle, and repeat modes.
 */
class PlayQueue {
    
    // Enum for repeat modes
    enum class RepeatMode {
        NONE,   // No repeat
        ONE,    // Repeat current song
        ALL     // Repeat entire queue
    }
    
    // State tracking
    private val queue: MutableList<Song> = mutableListOf()
    private var currentIndex: Int = 0
    private var shuffleEnabled: Boolean = false
    private var repeatMode: RepeatMode = RepeatMode.NONE
    
    // Store original order for shuffle/unshuffle
    private var originalQueue: List<Song> = emptyList()
    private var originalIndex: Int = 0
    
    /**
     * Replace the entire queue with a new list of songs.
     * @param songs The new list of songs
     * @param startIndex The index to start playing from (default: 0)
     */
    fun setQueue(songs: List<Song>, startIndex: Int = 0) {
        queue.clear()
        queue.addAll(songs)
        currentIndex = startIndex.coerceIn(0, maxOf(0, queue.size - 1))
        originalQueue = songs.toList()
        originalIndex = currentIndex
        shuffleEnabled = false
    }
    
    /**
     * Append a song to the end of the queue.
     * @param song The song to add
     */
    fun addToQueue(song: Song) {
        queue.add(song)
        if (shuffleEnabled) {
            originalQueue = originalQueue + song
        }
    }
    
    /**
     * Get the currently playing song.
     * @return The current song, or null if queue is empty
     */
    fun getCurrentSong(): Song? {
        return if (queue.isNotEmpty() && currentIndex in queue.indices) {
            queue[currentIndex]
        } else {
            null
        }
    }
    
    /**
     * Check if there is a next song available.
     * @return true if next song exists based on repeat mode
     */
    fun hasNext(): Boolean {
        return when {
            queue.isEmpty() -> false
            repeatMode == RepeatMode.ONE -> true
            repeatMode == RepeatMode.ALL -> true
            currentIndex < queue.size - 1 -> true
            else -> false
        }
    }
    
    /**
     * Move to the next song respecting repeat and shuffle modes.
     * @return The next song, or null if no next song available
     */
    fun next(): Song? {
        if (queue.isEmpty()) return null
        
        when (repeatMode) {
            RepeatMode.ONE -> {
                // Stay on current song
                return getCurrentSong()
            }
            RepeatMode.ALL -> {
                // Move to next, wrap around to start
                currentIndex = (currentIndex + 1) % queue.size
                return getCurrentSong()
            }
            RepeatMode.NONE -> {
                // Move to next if available
                if (currentIndex < queue.size - 1) {
                    currentIndex++
                    return getCurrentSong()
                }
                return null
            }
        }
    }
    
    /**
     * Check if there is a previous song available.
     * @return true if previous song exists
     */
    fun hasPrevious(): Boolean {
        return when {
            queue.isEmpty() -> false
            repeatMode == RepeatMode.ONE -> true
            repeatMode == RepeatMode.ALL -> true
            currentIndex > 0 -> true
            else -> false
        }
    }
    
    /**
     * Move to the previous song.
     * @return The previous song, or null if no previous song available
     */
    fun previous(): Song? {
        if (queue.isEmpty()) return null
        
        when (repeatMode) {
            RepeatMode.ONE -> {
                // Stay on current song
                return getCurrentSong()
            }
            RepeatMode.ALL -> {
                // Move to previous, wrap around to end
                currentIndex = if (currentIndex > 0) currentIndex - 1 else queue.size - 1
                return getCurrentSong()
            }
            RepeatMode.NONE -> {
                // Move to previous if available
                if (currentIndex > 0) {
                    currentIndex--
                    return getCurrentSong()
                }
                return null
            }
        }
    }
    
    /**
     * Remove a song from the queue at the specified index.
     * @param index The index of the song to remove
     * @return true if removal was successful
     */
    fun remove(index: Int): Boolean {
        if (index !in queue.indices) return false
        
        queue.removeAt(index)
        
        // Adjust current index if necessary
        when {
            queue.isEmpty() -> currentIndex = 0
            index < currentIndex -> currentIndex--
            index == currentIndex && currentIndex >= queue.size -> currentIndex = queue.size - 1
        }
        
        return true
    }
    
    /**
     * Randomize the queue order, keeping the current song at index 0.
     */
    fun shuffle() {
        if (queue.size <= 1) return
        
        // Save original order if not already shuffled
        if (!shuffleEnabled) {
            originalQueue = queue.toList()
            originalIndex = currentIndex
        }
        
        val currentSong = getCurrentSong()
        
        // Remove current song temporarily
        if (currentSong != null) {
            queue.removeAt(currentIndex)
        }
        
        // Shuffle remaining songs
        queue.shuffle()
        
        // Place current song at the beginning
        if (currentSong != null) {
            queue.add(0, currentSong)
        }
        
        currentIndex = 0
        shuffleEnabled = true
    }
    
    /**
     * Restore original queue order before shuffle.
     */
    fun unshuffle() {
        if (!shuffleEnabled) return
        
        val currentSong = getCurrentSong()
        queue.clear()
        queue.addAll(originalQueue)
        
        // Find the current song in the original queue
        currentIndex = if (currentSong != null) {
            queue.indexOf(currentSong).coerceAtLeast(0)
        } else {
            originalIndex
        }
        
        shuffleEnabled = false
    }
    
    /**
     * Toggle shuffle mode on/off.
     */
    fun toggleShuffle() {
        if (shuffleEnabled) {
            unshuffle()
        } else {
            shuffle()
        }
    }
    
    /**
     * Set the repeat mode.
     * @param mode The repeat mode to set
     */
    fun setRepeatMode(mode: RepeatMode) {
        repeatMode = mode
    }
    
    /**
     * Get the current repeat mode.
     * @return The current repeat mode
     */
    fun getRepeatMode(): RepeatMode = repeatMode
    
    /**
     * Toggle through repeat modes: NONE -> ALL -> ONE -> NONE
     */
    fun toggleRepeatMode() {
        repeatMode = when (repeatMode) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
    }
    
    /**
     * Get the current queue size.
     * @return The number of songs in the queue
     */
    fun size(): Int = queue.size
    
    /**
     * Get the current index.
     * @return The current index in the queue
     */
    fun getCurrentIndex(): Int = currentIndex
    
    /**
     * Check if shuffle is enabled.
     * @return true if shuffle is enabled
     */
    fun isShuffleEnabled(): Boolean = shuffleEnabled
    
    /**
     * Get a copy of the current queue.
     * @return A list copy of the current queue
     */
    fun getQueue(): List<Song> = queue.toList()
    
    /**
     * Clear the entire queue.
     */
    fun clear() {
        queue.clear()
        currentIndex = 0
        shuffleEnabled = false
        originalQueue = emptyList()
        repeatMode = RepeatMode.NONE
    }
}
