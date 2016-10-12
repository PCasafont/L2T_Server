/*

 */

package l2server.gameserver.gui.playertable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author KenM
 */
public class PlayerTableMouseListener implements MouseListener, ActionListener
{
	private JPopupMenu _popupMenu;
	private PlayerTablePane _parent;

	public PlayerTableMouseListener(PlayerTablePane parent)
	{
		_parent = parent;
		_popupMenu = new JPopupMenu();

		/*JMenuItem itemOpenGo = new JMenuItem("Do not show this");
		itemOpenGo.setActionCommand("mark");
		itemOpenGo.addActionListener(this);
		_popupMenu.add(itemOpenGo);

		JMenuItem itemOpen = new JMenuItem("Mark Yellow (What is this for??)");
		itemOpen.setActionCommand("yellow");
		itemOpen.addActionListener(this);
		_popupMenu.add(itemOpen);*/
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e)
	{
		checkPopup(e);
        /*if (e.isPopupTrigger())
		{
			showPopupMenu(e);
		}*/
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		/*if (e.getActionCommand().equals("yellow"))
		{
			JTable table = _parent.getPlayerTable();
			/*int row = _parent.getSelectedPacketindex();
			//int col = table.columnAtPoint(e.getPoint());
			boolean val = !((PacketTableModel) table.getModel()).getIsMarked(row);
			((PacketTableModel) table.getModel()).setIsMarked(row, val);
			table.repaint();
		}*/
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e)
	{
		checkPopup(e);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e)
	{

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e)
	{

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e)
	{
		checkPopup(e);
	}

	private void checkPopup(MouseEvent e)
	{
		_parent.setTableSelectByMouseEvent(e);
		if (e.isPopupTrigger())
		{
			_popupMenu.show(_parent.getPlayerTable(), e.getX(), e.getY());
		}
	}
}
