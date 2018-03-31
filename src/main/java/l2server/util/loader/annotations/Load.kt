package l2server.util.loader.annotations

/**
 * @author NosKun
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Load(val dependencies: Array<Dependency> = [])
