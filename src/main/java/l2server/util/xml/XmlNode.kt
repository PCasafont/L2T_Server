package l2server.util.xml

import org.w3c.dom.Node
import java.util.ArrayList
import java.util.function.Function
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * @author Pere
 */
class XmlNode internal constructor(private val base: Node) {

	val name: String = base.nodeName

	val text: String? = base.firstChild?.nodeValue

	val firstChild: XmlNode?
		get() {
			val children = getChildren()
			return if (children.isEmpty()) {
				null
			} else children[0]
		}

	fun hasAttributes() = base.attributes.length > 0

	fun hasAttribute(name: String) = base.attributes.getNamedItem(name) != null

	private fun getAttributeValue(name: String): String? = base.attributes.getNamedItem(name)?.nodeValue

	private fun <T> parse(name: String, value: String?, expectedType: Class<T>, parseFunction: Function<String, T>): T {
		if (value == null) {
			throw IllegalArgumentException(expectedType.simpleName + " value required for \"" + name + "\", but not specified.\r\nNode: " + this)
		}
		try {
			return parseFunction.apply(value)
		} catch (e: Exception) {
			throw IllegalArgumentException(expectedType.simpleName + " value required for \"" + name + "\", but found: " + value)
		}

	}

	private fun <T> parse(name: String, value: String?, expectedType: Class<T>, parseFunction: Function<String, T>, default: T): T {
		if (value == null) {
			return default
		}
		try {
			return parseFunction.apply(value)
		} catch (e: Exception) {
			throw IllegalArgumentException(expectedType.simpleName + " value required for \"" + name + "\", but found: " + value)
		}

	}

	fun getBool(name: String): Boolean {
		return parse(name, getAttributeValue(name), Boolean::class.java, Function { java.lang.Boolean.parseBoolean(it) })
	}

	fun getBool(name: String, default: Boolean): Boolean {
		return parse(name, getAttributeValue(name), Boolean::class.java, Function { java.lang.Boolean.parseBoolean(it) }, default)
	}

	fun getInt(name: String): Int {
		return parse(name, getAttributeValue(name), Int::class.java, Function { Integer.parseInt(it) })
	}

	fun getInt(name: String, default: Int): Int {
		return parse(name, getAttributeValue(name), Int::class.java, Function { Integer.parseInt(it) }, default)
	}

	fun getLong(name: String): Long {
		return parse(name, getAttributeValue(name), Long::class.java, Function { java.lang.Long.parseLong(it) })
	}

	fun getLong(name: String, default: Long): Long {
		return parse(name, getAttributeValue(name), Long::class.java, Function { java.lang.Long.parseLong(it) }, default)
	}

	fun getFloat(name: String): Float {
		return parse(name, getAttributeValue(name), Float::class.java, Function { java.lang.Float.parseFloat(it) })
	}

	fun getFloat(name: String, default: Float): Float {
		return parse(name, getAttributeValue(name), Float::class.java, Function { java.lang.Float.parseFloat(it) }, default)
	}

	fun getDouble(name: String): Double {
		return parse(name, getAttributeValue(name), Double::class.java, Function { java.lang.Double.parseDouble(it) })
	}

	fun getDouble(name: String, default: Double): Double {
		return parse(name, getAttributeValue(name), Double::class.java, Function { java.lang.Double.parseDouble(it) }, default)
	}

	fun getString(name: String): String {
		return parse(name, getAttributeValue(name), String::class.java, Function { it })
	}

	fun getString(name: String, default: String): String {
		return parse(name, getAttributeValue(name), String::class.java, Function { it }, default)
	}

	fun getAttributes(): Map<String, String> {
		val attributes = HashMap<String, String>()
		for (i in 0 until base.attributes.length) {
			val name = base.attributes.item(i).nodeName
			val value = base.attributes.item(i).nodeValue
			attributes[name] = value
		}
		return attributes
	}

	fun getChildren(): List<XmlNode> {
		val children = ArrayList<XmlNode>()
		var baseSubNode: Node? = base.firstChild
		while (baseSubNode != null) {
			if (baseSubNode.nodeType == Node.ELEMENT_NODE) {
				val child = XmlNode(baseSubNode)
				children.add(child)
			}
			baseSubNode = baseSubNode.nextSibling
		}
		return children
	}

	fun getChildren(name: String): List<XmlNode> {
		val list = ArrayList<XmlNode>()
		for (node in getChildren()) {
			if (node.name == name) {
				list.add(node)
			}
		}

		return list
	}

	fun getChild(name: String): XmlNode? {
		val children = getChildren(name)
		return if (children.isEmpty()) {
			null
		} else children[0]

	}

	override fun toString(): String {
		return toString(0)
	}

	private fun toString(tabDepth: Int): String {
		val tabsBuilder = StringBuilder()
		for (i in 0 until tabDepth) {
			tabsBuilder.append("\t")
		}

		val tabs = tabsBuilder.toString()
		val result = StringBuilder("$tabs<$name")
		for ((key, value) in getAttributes()) {
			result.append(" ").append(key).append("=\"").append(value).append("\"")
		}

		val children = getChildren()
		if (!children.isEmpty() || text != null && text.isNotEmpty()) {
			result.append(">\r\n")
			for (child in children) {
				result.append(child.toString(tabDepth + 1)).append("\r\n")
			}

			result.append(tabs).append("<").append(name).append(">\r\n")
		} else {
			result.append(" />")
		}

		return result.toString()
	}
}
