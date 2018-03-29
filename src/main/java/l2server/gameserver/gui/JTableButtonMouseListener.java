/*

 */

package l2server.gameserver.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

/**
 * @author KenM
 */
public class JTableButtonMouseListener implements MouseListener
{
	private JTable table;

	private void forwardEvent(MouseEvent e)
	{
		TableColumnModel columnModel = table.getColumnModel();
		int column = columnModel.getColumnIndexAtX(e.getX());
		int row = e.getY() / table.getRowHeight();
		Object value;
		JComponent c;

		if (row >= table.getRowCount() || row < 0 || column >= table.getColumnCount() || column < 0)
		{
			return;
		}

		value = table.getValueAt(row, column);

		if (!(value instanceof JComponent))
		{
			return;
		}

		c = (JComponent) value;

		if (c instanceof JButton)
		{
			JButton b = (JButton) c;
			if (e.getID() == MouseEvent.MOUSE_PRESSED)
			{
				b.doClick(100);
			}
		}
		c.dispatchEvent(new MouseEvent(c, e.getID(), e.getWhen(), e.getModifiers(), 0, 0, 1, e.isPopupTrigger(),
				e.getButton()));
	}

	public JTableButtonMouseListener(JTable table)
	{
		this.table = table;
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		forwardEvent(e);
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		forwardEvent(e);
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		forwardEvent(e);
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (e.getSource() instanceof JButton)
		{
			forwardEvent(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (e.getSource() instanceof JButton)
		{
			forwardEvent(e);
		}
	}
}
