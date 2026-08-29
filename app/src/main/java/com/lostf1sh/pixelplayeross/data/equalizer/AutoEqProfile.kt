package com.lostf1sh.pixelplayeross.data.equalizer

/**
 * A curated AutoEQ-style headphone correction profile.
 *
 * AuraPlay ships with correction curves for popular headphones and earphones.
 * The data is distilled from the public AutoEq project's Harman-target
 * corrections: each profile is the approximate inverse of the measured
 * frequency-response deviation, folded onto the 10 graphic-EQ bands
 * (31 Hz … 16 kHz) Android's equalizer exposes.
 *
 * These are *approximations* by design — unit-to-unit variance, fit and ear
 * anatomy all move the measured response. They get a listener into the right
 * ballpark; fine-tuning by ear is encouraged.
 */
data class AutoEqProfile(
    val id: String,
    val brand: String,
    val model: String,
    val form: Form,
    /** Clean gain applied before the EQ (negative = headroom). */
    val preampDb: Float,
    /** Band gains in dB at 31, 62, 125, 250, 500, 1k, 2k, 4k, 8k, 16 kHz. */
    val gains: List<Float>,
) {
    enum class Form { OVER_EAR, IN_EAR, EARBUD }

    /** Converts the profile into an [EqualizerPreset] (rounded to whole dB). */
    fun toPreset(): EqualizerPreset = EqualizerPreset(
        name = "autoeq_$id",
        displayName = model.uppercase(),
        bandLevels = gains.map { it.toInt().coerceIn(-15, 15) },
        isCustom = true
    )

    companion object {
        val ALL: List<AutoEqProfile> by lazy { buildLibrary() }

        fun byId(id: String): AutoEqProfile? = ALL.firstOrNull { it.id == id }

        private fun buildLibrary(): List<AutoEqProfile> = listOf(
            // ---------------------------------------------------- Over-ear
            AutoEqProfile(
                id = "hd600", brand = "Sennheiser", model = "HD 600", form = Form.OVER_EAR,
                preampDb = -5.5f,
                gains = listOf(5.5f, 4.5f, 2f, 0.5f, -0.5f, -0.5f, 1f, 2f, -1f, -1.5f)
            ),
            AutoEqProfile(
                id = "hd650", brand = "Sennheiser", model = "HD 650", form = Form.OVER_EAR,
                preampDb = -6f,
                gains = listOf(5.5f, 4.5f, 2f, 0.5f, -0.5f, -0.5f, 1f, 2f, -1f, -1.5f)
            ),
            AutoEqProfile(
                id = "hd660s2", brand = "Sennheiser", model = "HD 660S2", form = Form.OVER_EAR,
                preampDb = -4.5f,
                gains = listOf(4f, 3f, 1.5f, 0f, -1f, -1f, 0.5f, 1.5f, -1f, -1f)
            ),
            AutoEqProfile(
                id = "hd560s", brand = "Sennheiser", model = "HD 560S", form = Form.OVER_EAR,
                preampDb = -5f,
                gains = listOf(4.5f, 3.5f, 1.5f, 0f, -0.5f, 0f, 1f, 1.5f, -1.5f, -2f)
            ),
            AutoEqProfile(
                id = "hd800s", brand = "Sennheiser", model = "HD 800 S", form = Form.OVER_EAR,
                preampDb = -6f,
                gains = listOf(5.5f, 4.5f, 2f, 0f, -0.5f, -0.5f, 1.5f, 2f, -4f, -3f)
            ),
            AutoEqProfile(
                id = "momentum4", brand = "Sennheiser", model = "Momentum 4", form = Form.OVER_EAR,
                preampDb = -1.5f,
                gains = listOf(-2.5f, -1.5f, -0.5f, 0.5f, 1f, 0.5f, 1f, 0f, -2.5f, -2f)
            ),
            AutoEqProfile(
                id = "dt770pro", brand = "Beyerdynamic", model = "DT 770 Pro", form = Form.OVER_EAR,
                preampDb = -3.5f,
                gains = listOf(2f, 1f, 0.5f, 1.5f, 1f, 0f, -1.5f, -3f, -6f, -5f)
            ),
            AutoEqProfile(
                id = "dt990pro", brand = "Beyerdynamic", model = "DT 990 Pro", form = Form.OVER_EAR,
                preampDb = -4.5f,
                gains = listOf(3.5f, 2.5f, 1f, -0.5f, 0.5f, 1f, 0.5f, -2.5f, -8f, -6f)
            ),
            AutoEqProfile(
                id = "dt900prox", brand = "Beyerdynamic", model = "DT 900 Pro X", form = Form.OVER_EAR,
                preampDb = -4.5f,
                gains = listOf(4f, 3f, 1f, 0f, 0f, 0.5f, 1f, 0f, -3.5f, -2f)
            ),
            AutoEqProfile(
                id = "athm50x", brand = "Audio-Technica", model = "ATH-M50x", form = Form.OVER_EAR,
                preampDb = -3f,
                gains = listOf(3f, 1.5f, 0.5f, 1f, 1f, 0.5f, -0.5f, -1f, -3.5f, -1.5f)
            ),
            AutoEqProfile(
                id = "athr70x", brand = "Audio-Technica", model = "ATH-R70x", form = Form.OVER_EAR,
                preampDb = -5f,
                gains = listOf(4.5f, 3.5f, 1.5f, 0.5f, 0f, 0f, 1f, 1f, -1.5f, -2f)
            ),
            AutoEqProfile(
                id = "mdr7506", brand = "Sony", model = "MDR-7506", form = Form.OVER_EAR,
                preampDb = -2.5f,
                gains = listOf(2f, 1f, 0f, 1f, 1f, 0.5f, -0.5f, -1f, -3f, -2f)
            ),
            AutoEqProfile(
                id = "k371", brand = "AKG", model = "K371", form = Form.OVER_EAR,
                preampDb = -1.5f,
                gains = listOf(1f, 0.5f, 0.5f, 0f, 0.5f, 0f, 0f, 0.5f, -1f, -1f)
            ),
            AutoEqProfile(
                id = "k240studio", brand = "AKG", model = "K240 Studio", form = Form.OVER_EAR,
                preampDb = -6f,
                gains = listOf(5.5f, 4.5f, 2.5f, 0.5f, 1f, 1.5f, 0f, -0.5f, -2f, -1.5f)
            ),
            AutoEqProfile(
                id = "sundara", brand = "Hifiman", model = "Sundara", form = Form.OVER_EAR,
                preampDb = -5.5f,
                gains = listOf(5f, 4f, 2f, 0.5f, 0f, -0.5f, 0.5f, 1f, -1.5f, -1f)
            ),
            AutoEqProfile(
                id = "he400se", brand = "Hifiman", model = "HE400se", form = Form.OVER_EAR,
                preampDb = -6.5f,
                gains = listOf(6f, 5f, 2.5f, 0.5f, 0f, 0f, 1f, 0.5f, -1.5f, -1f)
            ),
            AutoEqProfile(
                id = "x2hr", brand = "Philips", model = "Fidelio X2HR", form = Form.OVER_EAR,
                preampDb = -2f,
                gains = listOf(1.5f, 1f, 0f, -2f, -1.5f, -0.5f, 0.5f, 0.5f, -2.5f, -2f)
            ),
            AutoEqProfile(
                id = "sr80x", brand = "Grado", model = "SR80x", form = Form.OVER_EAR,
                preampDb = -3.5f,
                gains = listOf(3f, 2f, 1f, 0f, 0.5f, 1f, 1.5f, 1f, -2f, -2f)
            ),
            AutoEqProfile(
                id = "meze99", brand = "Meze", model = "99 Classics", form = Form.OVER_EAR,
                preampDb = -1f,
                gains = listOf(-1f, -2f, -3f, -2.5f, -0.5f, 0.5f, 1f, 0f, -2f, -2f)
            ),
            // ---------------------------------------------------- Wireless ANC
            AutoEqProfile(
                id = "wh1000xm4", brand = "Sony", model = "WH-1000XM4", form = Form.OVER_EAR,
                preampDb = -1.5f,
                gains = listOf(-3.5f, -2.5f, -1.5f, 0.5f, 1f, 0.5f, 1.5f, 0.5f, -2f, -2.5f)
            ),
            AutoEqProfile(
                id = "wh1000xm5", brand = "Sony", model = "WH-1000XM5", form = Form.OVER_EAR,
                preampDb = -1.5f,
                gains = listOf(-3f, -2f, -1f, 0.5f, 1f, 0.5f, 1.5f, 0.5f, -2.5f, -2f)
            ),
            AutoEqProfile(
                id = "airpodsmax", brand = "Apple", model = "AirPods Max", form = Form.OVER_EAR,
                preampDb = -1.5f,
                gains = listOf(-1.5f, -1f, -0.5f, 0.5f, 1f, 0.5f, 0.5f, 1f, -1f, -1.5f)
            ),
            AutoEqProfile(
                id = "qc45", brand = "Bose", model = "QuietComfort 45", form = Form.OVER_EAR,
                preampDb = -1.5f,
                gains = listOf(-2.5f, -1.5f, -0.5f, 0f, 1f, 1f, 0.5f, 0f, -1.5f, -1f)
            ),
            // ---------------------------------------------------- In-ear
            AutoEqProfile(
                id = "chu2", brand = "Moondrop", model = "Chu II", form = Form.IN_EAR,
                preampDb = -1.5f,
                gains = listOf(1f, 0.5f, 0.5f, 0f, 0f, 0f, 0.5f, 0f, -1.5f, -1f)
            ),
            AutoEqProfile(
                id = "aria", brand = "Moondrop", model = "Aria", form = Form.IN_EAR,
                preampDb = -1f,
                gains = listOf(1f, 0.5f, 0.5f, 0f, 0f, 0f, 0.5f, 0f, -1f, -1f)
            ),
            AutoEqProfile(
                id = "timeless", brand = "7Hz", model = "Timeless", form = Form.IN_EAR,
                preampDb = -2f,
                gains = listOf(1.5f, 1f, 0.5f, 0f, 0f, 0f, 0.5f, 0.5f, -2f, -1.5f)
            ),
            AutoEqProfile(
                id = "zero", brand = "Truthear", model = "Zero", form = Form.IN_EAR,
                preampDb = -1f,
                gains = listOf(-1f, -0.5f, 0f, 0.5f, 0.5f, 0f, 0.5f, 0f, -2f, -2.5f)
            ),
            AutoEqProfile(
                id = "zs10pro", brand = "KZ", model = "ZS10 Pro", form = Form.IN_EAR,
                preampDb = -2f,
                gains = listOf(1.5f, 1f, 0.5f, 0f, 0f, -0.5f, -2f, -2f, -4f, -4.5f)
            ),
            AutoEqProfile(
                id = "ie200", brand = "Sennheiser", model = "IE 200", form = Form.IN_EAR,
                preampDb = -1.5f,
                gains = listOf(1f, 0.5f, 0.5f, 0f, 0f, 0f, 0.5f, 0.5f, -2f, -2f)
            ),
            AutoEqProfile(
                id = "er2se", brand = "Etymotic", model = "ER2SE", form = Form.IN_EAR,
                preampDb = -5.5f,
                gains = listOf(5f, 4f, 2f, 0f, 0f, 0f, 0.5f, 0.5f, -0.5f, -1f)
            ),
            AutoEqProfile(
                id = "airpodspro2", brand = "Apple", model = "AirPods Pro 2", form = Form.IN_EAR,
                preampDb = -1.5f,
                gains = listOf(-2f, -1.5f, -0.5f, 0.5f, 1f, 0.5f, 1f, 0.5f, -2f, -2.5f)
            ),
            AutoEqProfile(
                id = "wf1000xm4", brand = "Sony", model = "WF-1000XM4", form = Form.IN_EAR,
                preampDb = -1f,
                gains = listOf(-3.5f, -2.5f, -1.5f, 0f, 0.5f, 0f, 1f, -0.5f, -3f, -3f)
            ),
            AutoEqProfile(
                id = "buds2pro", brand = "Samsung", model = "Galaxy Buds2 Pro", form = Form.IN_EAR,
                preampDb = -1.5f,
                gains = listOf(-2.5f, -1.5f, -0.5f, 0f, 0.5f, 0.5f, 1f, 0.5f, -2.5f, -2f)
            ),
            AutoEqProfile(
                id = "fitpro", brand = "Beats", model = "Fit Pro", form = Form.IN_EAR,
                preampDb = -1f,
                gains = listOf(-4f, -3f, -1.5f, 0f, 0.5f, 0.5f, 1f, 0f, -2.5f, -2f)
            ),
            // ---------------------------------------------------- Earbuds & generic
            AutoEqProfile(
                id = "earpods", brand = "Apple", model = "EarPods (wired)", form = Form.EARBUD,
                preampDb = -1.5f,
                gains = listOf(0.5f, 0f, -0.5f, 0f, 0.5f, 1f, 1f, 0.5f, -1f, -1.5f)
            ),
            AutoEqProfile(
                id = "genericinear", brand = "Generic", model = "In-ear baseline", form = Form.IN_EAR,
                preampDb = -1f,
                gains = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -1f, -1f)
            ),
            AutoEqProfile(
                id = "genericoverear", brand = "Generic", model = "Over-ear baseline", form = Form.OVER_EAR,
                preampDb = -1.5f,
                gains = listOf(1f, 0.5f, 0.5f, 0f, 0f, 0f, 0.5f, 0.5f, -1.5f, -1f)
            ),
        )
    }
}
