package com.example.loginandregistration

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

/**
 * Glide configuration module for optimized image loading
 * Enables disk and memory caching for better performance
 */
@GlideModule
class GlideConfiguration : AppGlideModule() {
    
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Configure memory cache (10% of available memory)
        val memoryCacheSizeBytes = 1024 * 1024 * 20 // 20MB
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes.toLong()))
        
        // Configure disk cache (100MB)
        val diskCacheSizeBytes = 1024 * 1024 * 100 // 100MB
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes.toLong()))
        
        // Set default request options
        builder.setDefaultRequestOptions(
            RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        )
    }
    
    // Disable manifest parsing for better performance
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}
