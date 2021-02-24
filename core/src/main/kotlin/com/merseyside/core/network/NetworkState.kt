

package com.merseyside.core.network

/**
 * Different states for any network request.
 */
sealed class NetworkState {

    /**
     * Success network state.
     *
     * @param isAdditional Is additional request.
     * @param isEmptyResponse Is the body of response empty.
     */
     object Success : NetworkState()

    /**
     * Loading network state.
     *
     * @param isAdditional Is additional request.
     */
    object Loading: NetworkState()

    /**
     * Error network state.
     *
     * @param isAdditional Is additional request.
     */
    data class Error(
        val throwable: Throwable,
        val msg: String?
    ) : NetworkState()

    // ============================================================================================
    //  Public helpers methods
    // ============================================================================================

    /**
     * Check if current network state is [Success].
     *
     * @return True if is success state, otherwise false.
     */
    fun isSuccess() = this is Success

    /**
     * Check if current network state is [Loading].
     *
     * @return True if is loading state, otherwise false.
     */
    fun isLoading() = this is Loading

    /**
     * Check if current network state is [Error].
     *
     * @return True if is error state, otherwise false.
     */
    fun isError() = this is Error

}
