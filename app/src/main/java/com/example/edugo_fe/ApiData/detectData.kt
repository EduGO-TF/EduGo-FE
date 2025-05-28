package com.example.edugo_fe.ApiData

data class Detection(
    val class_id: Int,
    val confidence: Float,
    val height: Float,
    val width: Float,
    val x_center: Float,
    val y_center: Float
)

data class DetectionsResponse(
    val detections: List<Detection>
)