package l2server.util.xml

import org.w3c.dom.Node
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Pere
 */
class XmlDocument {

	val root: XmlNode

	constructor(file: File) {
		if (!file.exists()) {
			throw FileNotFoundException("The following XML could not be loaded: " + file.absolutePath)
		}

		val stream = FileInputStream(file)
		root = load(stream)
	}

	constructor(stream: InputStream) {
		root = load(stream)
	}

	fun getChildren(): List<XmlNode> = root.getChildren()

	private fun load(stream: InputStream): XmlNode {
		val doc = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().parse(stream)

		var baseNode: Node? = doc.firstChild
		while (baseNode != null) {
			if (baseNode.nodeType == Node.ELEMENT_NODE) {
				return XmlNode(baseNode)
			}
			baseNode = baseNode.nextSibling
		}

		throw RuntimeException("Tried to load an empty XML document!")
	}

	companion object {
		private val DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance()

		init {
			DOCUMENT_BUILDER_FACTORY.isValidating = false
			DOCUMENT_BUILDER_FACTORY.isIgnoringComments = true
		}
	}
}
