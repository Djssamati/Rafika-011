package com.camera.ta3bia

enum class Lang(val code: String, val label: String, val rtl: Boolean) {
    AR("ar", "العربية", true),
    FR("fr", "Français", false);

    companion object {
        fun from(code: String): Lang = values().firstOrNull { it.code == code } ?: AR
    }
}

/** All user-visible texts of the app, in Arabic and French. */
class Tr(val lang: Lang) {
    private fun t(ar: String, fr: String) = if (lang == Lang.AR) ar else fr

    // main screen
    val appTitle: String get() = t("تعبئة سهلة", "Recharge Facile")
    val step1: String get() = t("1) صوّر أرقام التعبئة (14 رقماً)", "1) Photographiez le code de recharge (14 chiffres)")
    val openCamera: String get() = t("فتح الكاميرا", "Ouvrir la caméra")
    val orWord: String get() = t("أو ", "ou ")
    val gallery: String get() = t("اختر من المعرض", "choisir dans la galerie")
    val step2: String get() = t("2) تأكد من الأرقام (14 رقماً)", "2) Vérifiez les chiffres (14 chiffres)")
    val digitsComplete: String get() = t("✓ الأرقام كاملة", "✓ Code complet")
    fun digitsCount(n: Int): String = t("عدد الأرقام: $n من 14", "Chiffres : $n sur 14")
    val step3: String get() = t("3) اضغط على نوع التعبئة", "3) Choisissez le type de recharge")
    val normal: String get() = t("تعبئة عادية", "Recharge normale")
    fun sendTo(target: String): String = t("الإرسال إلى: $target", "Envoi vers : $target")
    val sentNote: String
        get() = t(
            "✅ فُتحت الرسالة. اضغط «إرسال» ثم انتظر رسالة نجاح التعبئة.",
            "✅ Le SMS est ouvert. Appuyez sur « Envoyer » puis attendez le message de confirmation."
        )
    val settings: String get() = t("إعدادات", "Paramètres")

    // settings screen
    val settingsTitle: String get() = t("الإعدادات", "Paramètres")
    val languageSection: String get() = t("اللغة", "Langue")
    val interfaceSection: String get() = t("واجهة التطبيق", "Interface de l'application")
    val interfaceHint: String get() = t("اختر الشكل الذي يعجبك", "Choisissez l'apparence que vous préférez")
    val sendSection: String get() = t("الإرسال", "Envoi")
    val destLabel: String get() = t("رقم الإرسال", "Numéro d'envoi")
    val endCodeLabel: String
        get() = t("رمز في آخر الرسالة (اتركه فارغاً إن لم يلزم)", "Code en fin de message (laisser vide si inutile)")

    fun themeName(id: ThemeId): String = when (id) {
        ThemeId.ZELLIGE -> t("زخرفة مغربية", "Zellige")
        ThemeId.GLASS -> t("زجاجي", "Verre")
        ThemeId.POMEGRANATE -> t("الرمّان", "Grenade")
    }

    // OCR / camera / errors
    val ocrReading: String get() = t("⏳ جارٍ قراءة الأرقام…", "⏳ Lecture des chiffres…")
    val ocrOk: String
        get() = t("✓ تمت قراءة الأرقام بنجاح. تأكد منها ثم اختر النوع.", "✓ Chiffres lus avec succès. Vérifiez-les puis choisissez le type.")
    fun ocrPartial(n: Int): String =
        t("لم أقرأ كل الأرقام ($n/14). صوّر من جديد أو أكمل يدوياً.", "Lecture incomplète ($n/14). Reprenez la photo ou complétez à la main.")
    val ocrTooMany: String
        get() = t(
            "وجدت أكثر من 14 رقماً (رقم تسلسلي؟). ضع رقم التعبئة وحده داخل الإطار.",
            "Plus de 14 chiffres trouvés (numéro de série ?). Placez uniquement le code de recharge dans le cadre."
        )
    val ocrNone: String get() = t("لم أجد أرقاماً. صوّر من جديد أو اكتب يدوياً.", "Aucun chiffre trouvé. Reprenez la photo ou saisissez à la main.")
    val ocrFail: String get() = t("تعذّرت القراءة. صوّر من جديد أو اكتب يدوياً.", "Lecture impossible. Reprenez la photo ou saisissez à la main.")
    val camDenied: String
        get() = t("لم يتم السماح باستخدام الكاميرا. استخدم المعرض أو اكتب يدوياً.", "Accès à la caméra refusé. Utilisez la galerie ou saisissez à la main.")
    val imageFail: String get() = t("تعذّر فتح الصورة.", "Impossible d'ouvrir l'image.")
    val noSmsApp: String get() = t("لا يوجد تطبيق رسائل على الجهاز", "Aucune application de SMS sur l'appareil")
    val camGuide: String
        get() = t("ضع أرقام التعبئة (14 رقماً) داخل الإطار الأخضر", "Placez le code de recharge (14 chiffres) dans le cadre vert")
}
