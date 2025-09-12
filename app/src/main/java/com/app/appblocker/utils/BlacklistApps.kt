package com.app.appblocker.utils

object BlacklistApps {
    val packages = setOf(
        "com.app.appblocker", // This app

        // --- Sistema Android crítico ---
        "com.android.settings",
        "com.android.systemui",
        "com.android.launcher",
        "com.google.android.setupwizard",
        "com.google.android.apps.safetyhub",

        // --- Teléfono y contactos ---
        "com.android.phone",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.contacts",
        "com.google.android.contacts",
        "com.samsung.android.contacts",

        // --- SMS y mensajería básica ---
        "com.android.messaging",
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",

        // --- Tiendas oficiales ---
        "com.android.vending",               // Google Play Store
        "com.sec.android.app.samsungapps",   // Galaxy Store

        // --- Servicios Google ---
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.google.android.gsf.login",

        // --- OEM específicos (ejemplos) ---
        "com.huawei.android.launcher",
        "com.miui.home",
        "com.oppo.launcher",
        "com.sonymobile.home",

        // --- Utilitarios comunes que no suelen bloquearse ---
        "com.google.android.googlequicksearchbox", // Google App (búsqueda por voz, asistente)
        "com.android.camera",                      // Cámara AOSP
        "com.google.android.GoogleCamera",         // Cámara Google
        "com.sec.android.app.camera",              // Cámara Samsung
        "com.android.deskclock",                   // Reloj (alarmas, temporizador)
        "com.google.android.deskclock",
        "com.sec.android.app.clockpackage",        // Reloj Samsung
        "com.android.calculator2",                 // Calculadora básica
        "com.sec.android.app.popupcalculator",     // Calculadora Samsung
        "com.android.calendar",
        "com.google.android.calendar",
        "com.android.stk",

        // --- Operadores móviles (ejemplo: T-Mobile, AT&T, etc.) ---
        "com.tmobile.pr.adapt",
        "com.tmobile.simlock",
        "com.tmobile.pr.adapt.provider",
        "com.att.android.attsmartwifi",            // AT&T
        "com.verizon.mips.services"                // Verizon
    )
}