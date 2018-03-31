/*
 * Copyright (C) 2004-2016 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2server.util.loader

import l2server.util.ClassPathUtil
import l2server.util.TreeNode
import l2server.util.concurrent.ThreadPool
import l2server.util.loader.annotations.Load
import l2server.util.loader.annotations.Reload
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/**
 * @author NosKun
 */
object Loader {
	private val loadTrees = LinkedList<TreeNode<LoadHolder>>()
	private val reloads = HashMap<String, LoadHolder>()
	private val executionTimes = ConcurrentHashMap<LoadHolder, Long>()

	fun initialize(prefix: String) {
		try {
			for (loadMethod in ClassPathUtil.getAllMethodsAnnotatedWith(prefix, Load::class.java)) {
				val loadTreeNode = TreeNode(LoadHolder(findInstanceGetterMethod(loadMethod.declaringClass), loadMethod))
				val loadAnnotation = loadMethod.getAnnotation(Load::class.java)
				for (dependency in loadAnnotation.dependencies) {
					for (dependencyMethod in dependency.method) {
						try {
							val dependencyLoadHolder = LoadHolder(findInstanceGetterMethod(dependency.clazz.java),
									dependency.clazz.java.getDeclaredMethod(dependencyMethod))

							loadTreeNode.addChild(dependencyLoadHolder)

							if (loadTreeNode.children.stream().distinct().count() != loadTreeNode.children.size.toLong()) {
								throw RuntimeException("Duplicated Dependency " + dependencyLoadHolder + " on " + loadTreeNode.value)
							}
						} catch (e: NoSuchMethodException) {
							throw RuntimeException(
									"Dependency method for " + dependency.clazz.java.name + "." + dependencyMethod + "()" + " on " + loadTreeNode.value + " was not found.", e)
						}

					}
				}
				loadTrees.add(loadTreeNode)
			}
		} catch (e: Exception) {
			throw RuntimeException(e)
		}

		loadTrees.removeIf { loadTreeNode1 ->
			var wasAdopted = false
			for (loadTreeNode2 in loadTrees) {
				if (loadTreeNode1 !== loadTreeNode2) {
					val foundLoadTreeNodes = loadTreeNode2.findAll(loadTreeNode1.value)
					for (foundLoadTreeNode in foundLoadTreeNodes) {
						foundLoadTreeNode.addChildren(loadTreeNode1.children)
					}
					if (!foundLoadTreeNodes.isEmpty()) {
						wasAdopted = true
					}
				}
			}
			wasAdopted
		}

		for (loadTree in loadTrees) {
			val frontier = LinkedList<Deque<TreeNode<LoadHolder>>>()

			run {
				val deque = LinkedList<TreeNode<LoadHolder>>()
				deque.add(loadTree)
				frontier.offer(deque)
			}

			while (!frontier.isEmpty()) {
				val front = frontier.poll()
				for (loadTreeNode in front.last.children) {
					if (front.contains(loadTreeNode)) {
						front.add(loadTreeNode)
						while (front.first.value != front.last.value) {
							front.removeFirst()
						}
						throw RuntimeException("Cyclic Dependency found [${front.joinToString(" -> ") { it.value.toString() }}]")
					}
					val deque = LinkedList<TreeNode<LoadHolder>>()
					deque.addAll(front)
					deque.add(loadTreeNode)
					frontier.offer(deque)
				}
			}
		}

		try {
			for (reloadMethod in ClassPathUtil.getAllMethodsAnnotatedWith(prefix, Reload::class.java)) {
				val reloadName = reloadMethod.getAnnotation(Reload::class.java).value
				val loadHolder = LoadHolder(findInstanceGetterMethod(reloadMethod.declaringClass), reloadMethod)
				val previousLoadHolder = reloads.putIfAbsent(reloadName, loadHolder)
				if (previousLoadHolder != null) {
					throw RuntimeException(
							"More than one Reload with name $reloadName found [$previousLoadHolder, $loadHolder]")
				}
			}
		} catch (e: Exception) {
			throw RuntimeException(e)
		}
	}

	fun runAsync(): CompletableFuture<Void> {
		val loadGroupCompletableFutures = LinkedList<CompletableFuture<Void>>()
		val previousLoadGroupCompletableFuture = loadGroupCompletableFutures.peekLast()
		val completableFutures = HashMap<LoadHolder, CompletableFuture<Void>>()
		val rootCompletableFutures = LinkedList<CompletableFuture<Void>>()
		for (loadTreeNode in loadTrees) {
			var lastCompletableFuture: CompletableFuture<Void>? = null
			for (treeNode in loadTreeNode.postOrderTraversal()) {
				lastCompletableFuture = completableFutures.getOrPut(treeNode.value) {
					var dependencyCompletableFutures = treeNode.children
							.map { it.value }
							.map { completableFutures[it] }
					if (previousLoadGroupCompletableFuture != null) {
						dependencyCompletableFutures += previousLoadGroupCompletableFuture
					}
					CompletableFuture.allOf(*dependencyCompletableFutures.toTypedArray())
							.thenRunAsync(Runnable { runTreeNode(treeNode) }, ThreadPool.getThreadPoolExecutor())
				}
			}
			rootCompletableFutures.add(lastCompletableFuture!!)
		}
		loadGroupCompletableFutures.add(CompletableFuture.allOf(*rootCompletableFutures.toTypedArray()))

		return CompletableFuture.allOf(*loadGroupCompletableFutures.toTypedArray())
	}

	fun run() {
		val runNodes = HashSet<LoadHolder>()
		for (loadTreeNode in loadTrees) {
			for (treeNode in loadTreeNode.postOrderTraversal()) {
				if (runNodes.add(treeNode.value)) {
					runTreeNode(treeNode)
				}
			}
		}
	}

	@Throws(RuntimeException::class)
	private fun runTreeNode(treeNode: TreeNode<LoadHolder>) {
		try {
			val startTime = System.nanoTime()
			treeNode.value.call()
			executionTimes[treeNode.value] = (treeNode.children.map { executionTimes[it.value] ?: 0 }.max() ?: 0) + (System.nanoTime() - startTime)
		} catch (e: IllegalAccessException) {
			throw RuntimeException("Calling " + treeNode.value + " failed", e)
		} catch (e: InvocationTargetException) {
			throw RuntimeException("Calling " + treeNode.value + " failed", e)
		}

	}

	fun getDependencyTreeString(): String {
		val sj = StringJoiner(System.lineSeparator())
		val loadTreesIterator = loadTrees.sortedBy { executionTimes[it.value] ?: 0L }.iterator()
		while (loadTreesIterator.hasNext()) {
			getDependencyTreeString(sj, loadTreesIterator.next(), "", !loadTreesIterator.hasNext())
		}
		return sj.toString()
	}

	private fun getDependencyTreeString(sj: StringJoiner, treeNode: TreeNode<LoadHolder>, indent: String, lastChild: Boolean) {
		sj.add(indent + (if (lastChild) "\\" else "+") + "--- " + treeNode.value + " " +
				TimeUnit.NANOSECONDS.toMillis(executionTimes.getOrDefault(treeNode.value, -1L)) + " ms")
		val childrenIterator = treeNode.children
				.stream()
				.sorted(Comparator.comparingLong<TreeNode<LoadHolder>> { executionTimes.getOrDefault(it.value, 0L) }.reversed())
				.iterator()
		while (childrenIterator.hasNext()) {
			getDependencyTreeString(sj, childrenIterator.next(), indent + (if (lastChild) " " else "|") + "    ", !childrenIterator.hasNext())
		}
	}

	@Throws(IOException::class)
	fun writeDependencyTreeToFile(path: Path) {
		Files.write(path, getDependencyTreeString().toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
	}

	fun getReloads(): Map<String, LoadHolder> {
		return reloads
	}

	private fun findInstanceGetterMethod(clazz: Class<*>): Method {
		val instanceGetterMethods = Arrays.stream(clazz.declaredMethods)
				.filter { it.parameterCount == 0 && it.name == "getInstance" }
				.collect(Collectors.toList())
		if (instanceGetterMethods.isEmpty()) {
			throw UnsupportedOperationException(
					"$clazz contains Load annotated method(s) but it does not have an InstanceGetter annotated method.")
		}

		if (instanceGetterMethods.size != 1) {
			throw UnsupportedOperationException("There should be only one InstanceGetter annotated method $instanceGetterMethods")
		}

		if (!Modifier.isPublic(instanceGetterMethods[0].modifiers)) {
			throw UnsupportedOperationException("non public InstanceGetter method ${instanceGetterMethods[0]}")
		}

		if (!Modifier.isStatic(instanceGetterMethods[0].modifiers)) {
			throw UnsupportedOperationException("non static InstanceGetter method ${instanceGetterMethods[0]}")
		}
		return instanceGetterMethods[0]
	}
}
