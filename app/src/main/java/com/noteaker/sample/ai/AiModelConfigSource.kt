package com.noteaker.sample.ai

import com.noteaker.sample.BuildConfig
import javax.inject.Inject


/**
 * Supplies the current [AiModelType]. Replace the default implementation to choose
 * based on connectivity, user preference, or feature flags.
 */
interface AiModelConfigSource {

    fun current(): AiModelType
}

/**
 * Default [AiModelConfigSource] driven by [BuildConfig.AI_NAVIGATION_USE_LOCAL].
 * Set that flag (e.g. in build.gradle buildTypes or productFlavors) or replace this
 * binding with one that checks connectivity / user preference.
 */
class DefaultAiModelConfigSource @Inject constructor() : AiModelConfigSource {

    override fun current(): AiModelType = AiModelType.CLOUD
}

/**
 * Which AI backend to use for navigation intent resolution.
 * Switch at runtime (e.g. based on connectivity) by providing a different [AiModelConfigSource].
 */
enum class AiModelType {
    /** Cloud: Firebase AI (Gemini). Requires network. */
    CLOUD,

    /** Local: ML Kit GenAI Prompt API (Gemini Nano on-device). No network. */
    LOCAL
}

