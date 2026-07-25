import re
with open('app/src/main/java/com/example/network/SessionManager.kt', 'r') as f:
    content = f.read()

target = """    fun getAllowNotifications(): Boolean {
        return prefs.getBoolean("KEY_ALLOW_NOTIFICATIONS", true)
    }
}"""

replacement = """    fun getAllowNotifications(): Boolean {
        return prefs.getBoolean("KEY_ALLOW_NOTIFICATIONS", true)
    }

    fun setHasSeenTutorial(hasSeen: Boolean) {
        prefs.edit().putBoolean("KEY_HAS_SEEN_TUTORIAL", hasSeen).apply()
    }
    
    fun hasSeenTutorial(): Boolean {
        return prefs.getBoolean("KEY_HAS_SEEN_TUTORIAL", false)
    }
}"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/network/SessionManager.kt', 'w') as f:
    f.write(content)
