package com.github.zakaprov.interartive.domain

import com.google.ar.core.AugmentedImage
import com.google.ar.core.HitResult

interface ArCoreSessionListener {

    fun onSurfaceClicked(results: List<HitResult>)

    fun onAugmentedImagesFound(images: Collection<AugmentedImage>)
}