package com.onus.demotest

import android.content.Context
import android.util.Log
import androidx.startup.Initializer

class FoldableSplitInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        runCatching {
            val controllerClass = Class.forName("androidx.window.embedding.RuleController")
            val getInstance = controllerClass.getMethod("getInstance", Context::class.java)
            val parseRules = controllerClass.getMethod("parseRules", Context::class.java, Int::class.javaPrimitiveType)
            val setRules = controllerClass.getMethod("setRules", Set::class.java)
            val controller = getInstance.invoke(null, context)
            val rules = parseRules.invoke(null, context, R.xml.foldable_activity_embedding_rules)
            setRules.invoke(controller, rules)
        }.onFailure {
            Log.d("FoldableDemo", "Activity embedding rules unavailable", it)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
