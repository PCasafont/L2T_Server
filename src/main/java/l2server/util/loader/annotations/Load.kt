package l2server.util.loader.annotations

import kotlin.reflect.KClass

/**
 * @author NosKun
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Load(val dependencies: Array<KClass<*>> = [])
