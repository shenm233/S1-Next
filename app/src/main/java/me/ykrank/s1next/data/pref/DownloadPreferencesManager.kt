package me.ykrank.s1next.data.pref

import android.support.annotation.IntDef
import com.bumptech.glide.load.Key
import com.bumptech.glide.signature.ObjectKey
import me.ykrank.s1next.data.Wifi
import me.ykrank.s1next.util.DateUtil

/**
 * A manager manage the download preferences that are associated with settings.
 */
class DownloadPreferencesManager(private val mPreferencesProvider: DownloadPreferences, private val mWifi: Wifi) {

    val totalImageCacheSize: Int
        get() = TotalDownloadCacheSize.getByte(mPreferencesProvider.totalImageCacheSizeIndex)

    val totalDataCacheSize: Int
        get() = TotalDownloadCacheSize.getMByte(mPreferencesProvider.totalDataCacheSizeIndex)

    val isAvatarsDownload: Boolean
        get() = DownloadStrategyInternal.isDownload(mPreferencesProvider.avatarsDownloadStrategyIndex, mWifi.isWifiEnabled)

    val isHighResolutionAvatarsDownload: Boolean
        get() = AvatarResolutionStrategy.isHigherResolutionDownload(mPreferencesProvider.avatarResolutionStrategyIndex, mWifi.isWifiEnabled)

    val avatarCacheInvalidationIntervalSignature: Key
        get() = AvatarCacheInvalidationInterval.getSignature(mPreferencesProvider.avatarCacheInvalidationInterval)

    /**
     * Checks whether we need to download images.
     */
    val isImagesDownload: Boolean
        get() = DownloadStrategyInternal.isDownload(mPreferencesProvider.imagesDownloadStrategyIndex, mWifi.isWifiEnabled)

    /**
     * Checks whether we need to monitor the Wi-Fi status.
     * We needn't monitor the Wi-Fi status if we needn't/should
     * download avatars or images.
     */
    fun needMonitorWifi(): Boolean {
        val avatarDownloadStrategy = mPreferencesProvider.avatarsDownloadStrategyIndex.toLong()
        val imageDownloadStrategy = mPreferencesProvider.imagesDownloadStrategyIndex.toLong()
        return avatarDownloadStrategy == DownloadStrategyInternal.WIFI || imageDownloadStrategy == DownloadStrategyInternal.WIFI
    }

    private object TotalDownloadCacheSize {
        private val LOW = 32
        private val NORMAL = 64
        private val HIGH = 128

        private val SIZE = intArrayOf(LOW, NORMAL, HIGH)

        fun getByte(index: Int): Int {
            return SIZE[index] * 1000 * 1000
        }

        fun getMByte(index: Int): Int {
            return if (index < 0 || index >= SIZE.size) SIZE[0] else SIZE[index]
        }
    }

    private object DownloadStrategyInternal {
        const val NOT = 0L
        const val WIFI = 1L
        const val ALWAYS = 2L

        @IntDef(NOT, WIFI, ALWAYS)
        @Retention(AnnotationRetention.SOURCE)
        internal annotation class DownloadStrategy

        fun isDownload(@DownloadStrategy downloadStrategy: Int, hasWifi: Boolean): Boolean {
            return downloadStrategy.toLong() == WIFI && hasWifi || downloadStrategy.toLong() == ALWAYS
        }
    }

    private object AvatarResolutionStrategy {
        const val LOW = 0L
        const val HIGH_WIFI = 1L
        const val HIGH = 2L

        @IntDef(LOW, HIGH_WIFI, HIGH)
        @Retention(AnnotationRetention.SOURCE)
        internal annotation class AvatarStrategy

        fun isHigherResolutionDownload(@AvatarStrategy avatarResolutionStrategy: Int, hasWifi: Boolean): Boolean {
            return avatarResolutionStrategy.toLong() == HIGH_WIFI && hasWifi || avatarResolutionStrategy.toLong() == HIGH
        }
    }

    private object AvatarCacheInvalidationInterval {
        private val EVERY_DAY = DateUtil.today()
        private val EVERY_WEEK = DateUtil.dayOfWeek()
        private val EVERY_MONTH = DateUtil.dayOfMonth()

        private val VALUES = arrayOf(EVERY_DAY, EVERY_WEEK, EVERY_MONTH)

        /**
         * Gets a string signature in order to invalidate avatar every day/week/month.

         * @return A [Key] representing the string signature
         * * of date that will be mixed in to the cache key.
         */
        fun getSignature(index: Int): Key {
            return ObjectKey(if (index < 0 || index >= VALUES.size) VALUES[0] else VALUES[index])
        }
    }
}
