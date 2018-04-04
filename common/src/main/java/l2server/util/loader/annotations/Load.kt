package l2server.util.loader.annotations

import kotlin.reflect.KClass

/**
 * @author NosKun
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Load(val main: Boolean = true, val dependencies: Array<KClass<*>> = [])
