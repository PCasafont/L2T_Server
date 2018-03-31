package l2server.util.loader.annotations

import kotlin.reflect.KClass

/**
 * @author NosKun
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class Dependency(val clazz: KClass<*>,
							val method: Array<String> = ["load"])
