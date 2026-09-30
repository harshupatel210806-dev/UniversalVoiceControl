package com.patel.universalvoicecontrol

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import java.util.Locale

object VoiceCommandParser {
    fun parse(context: Context, raw: String): String {
        val text = raw.trim()
        if (text.isBlank()) return "No command heard"
        val normalized = normalize(text)
        val lower = normalized.lowercase(Locale.getDefault())
        val service = UniversalAccessibilityService.instance
            ?: return "Accessibility Service ચાલુ નથી."

        // Common multi-step commands. We intentionally stop before destructive actions.
        if (lower.contains(" and search ") || lower.contains(" ane search ") || lower.contains(" અને search ")) {
            val openPart = lower.substringBefore(" and search ").ifBlank {
                lower.substringBefore(" ane search ").ifBlank { lower.substringBefore(" અને search ") }
            }
            val query = when {
                lower.contains(" and search ") -> text.substringAfter(" and search ", "")
                lower.contains(" ane search ") -> text.substringAfter(" ane search ", "")
                else -> text.substringAfter(" અને search ", "")
            }.trim()
            if (openPart.startsWith("open ") && query.isNotBlank()) {
                val app = openPart.removePrefix("open ").trim()
                if (openApp(context, app)) {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))))
                    return "Opening $app and searching $query"
                }
            }
        }

        when {
            lower in setOf("back", "go back", "પાછળ", "પાછા", "પાછળ જા", "પાછા જા", "पीछे", "पीछे जाओ") -> {
                service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
                return "Back"
            }
            lower in setOf("home", "go home", "હોમ", "હોમ પર જા", "घर", "होम") -> {
                service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME)
                return "Home"
            }
            lower.contains("recent") || lower.contains("રીસેન્ટ") || lower.contains("રીસન્ટ") || lower.contains("हाल के ऐप") -> {
                service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS)
                return "Recent Apps"
            }
            lower.contains("scroll down") || lower.contains("સ્ક્રોલ ડાઉન") || lower.contains("નીચે સ્ક્રોલ") || lower.contains("नीचे स्क्रोल") -> {
                service.scroll(true)
                return "Scroll Down"
            }
            lower.contains("scroll up") || lower.contains("સ્ક્રોલ અપ") || lower.contains("ઉપર સ્ક્રોલ") || lower.contains("ऊपर स्क्रोल") -> {
                service.scroll(false)
                return "Scroll Up"
            }
            lower.startsWith("tap ") || lower.startsWith("ટેપ ") || lower.startsWith("ટેપ કરો ") || lower.startsWith("टैप ") -> {
                val target = valueAfterCommand(text, lower, "tap", "ટેપ", "ટેપ કરો", "टैप")
                return if (service.tapText(target)) "Tapped: $target" else "Could not find: $target"
            }
            lower.startsWith("type ") || lower.startsWith("લખ ") || lower.startsWith("લખો ") || lower.startsWith("લખો ") || lower.startsWith("टाइप ") -> {
                val value = valueAfterCommand(text, lower, "type", "લખ", "લખો", "टाइप")
                return if (service.typeText(value)) "Typed" else "Text field not found"
            }
            lower.startsWith("open ") || lower.startsWith("ઓપન ") || lower.startsWith("ખોલ ") || lower.startsWith("ખોલો ") || lower.startsWith("खोलो ") || lower.startsWith("खोल ") -> {
                val appName = valueAfterCommand(text, lower, "open", "ઓપન", "ખોલ", "ખોલો", "खोलो", "खोल")
                return if (openApp(context, appName)) "Opening $appName" else "App not found: $appName"
            }
        }

        if (lower.startsWith("search ") || lower.startsWith("સર્ચ ") || lower.startsWith("શોધ ") || lower.startsWith("search karo ") || lower.startsWith("सर्च ")) {
            val q = valueAfterCommand(text, lower, "search", "સર્ચ", "શોધ", "search karo", "सर्च").trim()
            if (q.isNotBlank()) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(q))))
                return "Searching: $q"
            }
        }

        return "Command not understood: $text"
    }

    private fun valueAfterCommand(original: String, lower: String, vararg commands: String): String {
        val cmd = commands.firstOrNull { lower.startsWith("$it ") || lower == it } ?: return original
        return original.substring(cmd.length).trim()
    }

    private fun normalize(input: String): String {
        return input.trim()
            .replace(Regex("\\s+"), " ")
            .replace("व्हाट्सऐप", "WhatsApp", true)
            .replace("व्हाट्सएप", "WhatsApp", true)
            .replace("ઇન્સ્ટાગ્રામ", "Instagram", true)
            .replace("યુટ્યુબ", "YouTube", true)
    }

    private fun openApp(context: Context, spokenName: String): Boolean {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val target = spokenName.lowercase(Locale.getDefault()).trim()
        if (target.isBlank()) return false

        val exact = apps.firstOrNull {
            pm.getApplicationLabel(it).toString().lowercase(Locale.getDefault()) == target
        }
        val partial = apps.firstOrNull {
            pm.getApplicationLabel(it).toString().lowercase(Locale.getDefault()).contains(target)
        }
        val match = exact ?: partial ?: knownAlias(pm, apps, target) ?: return false
        val launch = pm.getLaunchIntentForPackage(match.packageName) ?: return false
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
        return true
    }

    private fun knownAlias(pm: PackageManager, apps: List<android.content.pm.ApplicationInfo>, target: String): android.content.pm.ApplicationInfo? {
        val aliases = mapOf(
            "વોટ્સએપ" to "whatsapp", "વોટ્સ એપ" to "whatsapp", "wa" to "whatsapp",
            "ઇન્સ્ટા" to "instagram", "ઇન્સ્ટાગ્રામ" to "instagram",
            "યુટ્યુબ" to "youtube", "યુટ્યુબ" to "youtube",
            "ક્રોમ" to "chrome", "કેમેરા" to "camera", "ગેલેરી" to "gallery",
            "whatsapp" to "whatsapp", "instagram" to "instagram", "youtube" to "youtube",
            "chrome" to "chrome", "camera" to "camera", "gallery" to "gallery"
        )
        val wanted = aliases[target] ?: return null
        return apps.firstOrNull { pm.getApplicationLabel(it).toString().lowercase(Locale.getDefault()).contains(wanted) }
    }
}
