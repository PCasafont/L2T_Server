package l2server.util;

import com.google.common.collect.TreeTraverser;

import java.util.*;

/**
 * @author NosKun
 * @param <T>
 */
public class TreeNode<T> implements Iterable<TreeNode<T>>
{
	private TreeNode<T> _parent;
	private final List<TreeNode<T>> _children = new LinkedList<>();
	private T _value;
	
	public TreeNode(T value)
	{
		_value = value;
	}
	
	public TreeNode<T> getParent()
	{
		return _parent;
	}
	
	public List<TreeNode<T>> getChildren()
	{
		return _children;
	}
	
	public TreeNode<T> addChild(T value)
	{
		final TreeNode<T> childNode = new TreeNode<>(value);
		childNode._parent = this;
		_children.add(childNode);
		return childNode;
	}
	
	public void addChildren(Collection<TreeNode<T>> children)
	{
		for (TreeNode<T> child : children)
		{
			addChild(child.getValue()).addChildren(child.getChildren());
		}
	}
	
	public T getValue()
	{
		return _value;
	}
	
	public void setValue(T value)
	{
		_value = value;
	}
	
	@Override
	public Iterator<TreeNode<T>> iterator()
	{
		return breadthFirstTraversal().iterator();
	}
	
	public Iterable<TreeNode<T>> breadthFirstTraversal()
	{
		return TreeTraverser.<TreeNode<T>> using(n -> n.getChildren()).breadthFirstTraversal(this);
	}
	
	public Iterable<TreeNode<T>> preOrderTraversal()
	{
		return TreeTraverser.<TreeNode<T>> using(n -> n.getChildren()).preOrderTraversal(this);
	}
	
	public Iterable<TreeNode<T>> postOrderTraversal()
	{
		return TreeTraverser.<TreeNode<T>> using(n -> n.getChildren()).postOrderTraversal(this);
	}
	
	public List<TreeNode<T>> findAll(T value)
	{
		final List<TreeNode<T>> treeNodes = new LinkedList<>();
		for (TreeNode<T> treeNode : this)
		{
			if (treeNode.getValue().equals(value))
			{
				treeNodes.add(treeNode);
			}
		}
		return treeNodes;
	}
	
	public boolean contains(T value)
	{
		for (TreeNode<T> treeNode : this)
		{
			if (treeNode.getValue().equals(value))
			{
				return true;
			}
		}
		return false;
	}
	
	public Queue<TreeNode<T>> getLeafNodes()
	{
		final Queue<TreeNode<T>> leafNodes = new LinkedList<>();
		for (TreeNode<T> treeNode : this)
		{
			if (treeNode.getChildren().isEmpty())
			{
				leafNodes.add(treeNode);
			}
		}
		return leafNodes;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if ((o == null) || (getClass() != o.getClass()))
		{
			return false;
		}
		final TreeNode<?> treeNode = (TreeNode<?>) o;
		return _value != null ? _value.equals(treeNode._value) : treeNode._value == null;
	}
	
	@Override
	public int hashCode()
	{
		return _value != null ? _value.hashCode() : 0;
	}
}
