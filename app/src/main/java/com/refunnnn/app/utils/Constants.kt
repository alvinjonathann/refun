package com.refunnnn.app.utils

object Constants {
    // Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_HISTORY = "history"

    // User fields
    const val FIELD_NAME = "name"
    const val FIELD_EMAIL = "email"
    const val FIELD_POINTS = "points"
    const val FIELD_CREATED_AT = "createdAt"

    // History fields
    const val FIELD_BARCODE = "barcode"
    const val FIELD_TIMESTAMP = "timestamp"

    // Points
    const val POINTS_PER_SCAN = 10

    // Preferences
    const val PREF_NAME = "ReFunPrefs"
    const val PREF_IS_FIRST_LAUNCH = "isFirstLaunch"

    // Request codes
    const val REQUEST_CAMERA_PERMISSION = 100
    const val REQUEST_IMAGE_CAPTURE = 101

    // Bundle keys
    const val KEY_USER_ID = "userId"
    const val KEY_BARCODE = "barcode"
    const val KEY_POINTS = "points"

    // Error messages
    const val ERROR_CAMERA_PERMISSION = "Camera permission is required for barcode scanning"
    const val ERROR_CAMERA_INIT = "Failed to initialize camera"
    const val ERROR_BARCODE_SCAN = "Failed to scan barcode"
    const val ERROR_NETWORK = "Network error. Please check your connection"
    const val ERROR_AUTHENTICATION = "Authentication failed"
    const val ERROR_DATABASE = "Database operation failed"

    // Success messages
    const val SUCCESS_SCAN = "Barcode scanned successfully"
    const val SUCCESS_POINTS_ADDED = "Points added successfully"
    const val SUCCESS_REGISTRATION = "Registration successful"
    const val SUCCESS_LOGIN = "Login successful"
} 