package com.github.zakaprov.interartive.domain

import com.google.ar.core.HitResult

interface SurfaceClickListener {

    fun onSurfaceClicked(results: List<HitResult>)
}