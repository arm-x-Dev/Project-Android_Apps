package com.example.wifiinspectorpro.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Modern Minimalist Palette (Slate & Indigo)
// ==========================================

// --- Dark Mode Tokens ---
val SlateBgDark = Color(0xFF0B0A0F)         // Deep slate-black background
val SurfaceLowDark = Color(0xFF16151A)      // Level 1 Cards / Flat surfaces
val SurfaceBrightDark = Color(0xFF22212A)   // Level 3 Active / Focused surfaces
val SurfaceLowestDark = Color(0xFF09080C)   // Deep recessed areas (inputs)
val IndigoPrimaryDark = Color(0xFF6366F1)   // Electric Indigo Brand Accent
val IndigoSecondaryDark = Color(0xFF818CF8) // Soft Indigo supporting tone

// --- Light Mode Tokens ---
val SlateBgLight = Color(0xFFF8FAFC)        // Off-white / light slate background
val SurfaceLowLight = Color(0xFFFFFFFF)     // Pure white cards / flat surfaces
val SurfaceBrightLight = Color(0xFFF1F5F9)  // Level 3 Active / Focused surfaces
val SurfaceLowestLight = Color(0xFFE2E8F0)  // Deep recessed areas (inputs)
val IndigoPrimaryLight = Color(0xFF4F46E5)  // Deep Royal Indigo Brand Accent
val IndigoSecondaryLight = Color(0xFF6366F1)// Standard Indigo supporting tone

// --- Shared Dynamic Helpers ---
val TextPrimaryDark = Color(0xFFFFFFFF)     // Crisp white text
val TextSecondaryDark = Color(0xFF94A3B8)   // Soft Slate/Cool Gray text
val TextMutedDark = Color(0xFF475569)        // Slate Gray metadata

val TextPrimaryLight = Color(0xFF0F172A)    // Slate-900 heading text
val TextSecondaryLight = Color(0xFF475569)  // Slate-600 body text
val TextMutedLight = Color(0xFF94A3B8)      // Cool Gray metadata

// ==========================================
// Backward-Compatible Mappings (Prevents Builds from Breaking)
// ==========================================
val ObsidianBg = SlateBgDark
val KineticLavender = IndigoPrimaryDark
val PulseCoral = IndigoSecondaryDark
val ShadowLavender = TextSecondaryDark

val SurfaceLow = SurfaceLowDark
val SurfaceBright = SurfaceBrightDark
val SurfaceLowest = SurfaceLowestDark

val GradientStart = IndigoPrimaryDark
val GradientEnd = IndigoPrimaryDark