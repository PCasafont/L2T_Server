/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.model;

import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.templates.chars.L2PcTemplate;

/**
 * Used to Store data sent to Client for Character
 * Selection screen.
 *
 * @version $Revision: 1.2.2.2.2.4 $ $Date: 2005/03/27 15:29:33 $
 */
public class CharSelectInfoPackage {
	private String name;
	private int objectId = 0;
	private int charId = 0x00030b7a;
	private long exp = 0;
	private long sp = 0;
	private int clanId = 0;
	private L2PcTemplate template = null;
	private int classId = 0;
	private long deleteTimer = 0L;
	private long lastAccess = 0L;
	private int face = 0;
	private int hairStyle = 0;
	private int hairColor = 0;
	private int sex = 0;
	private int level = 1;
	private int maxHp = 0;
	private double currentHp = 0;
	private int maxMp = 0;
	private double currentMp = 0;
	private int[][] paperdoll;
	private int reputation = 0;
	private int pkKills = 0;
	private int pvpKills = 0;
	private long augmentationId = 0;
	private int transformId = 0;
	private int x = 0;
	private int y = 0;
	private int z = 0;
	private boolean showHat = true;
	private int vitalityPoints = 0;
	private int vitalityLevel = 0;
	
	/**
	 */
	public CharSelectInfoPackage(int objectId, String name) {
		setObjectId(objectId);
		this.name = name;
		paperdoll = PcInventory.restoreVisibleInventory(objectId);
	}
	
	public int getObjectId() {
		return objectId;
	}
	
	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}
	
	public int getCharId() {
		return charId;
	}
	
	public void setCharId(int charId) {
		this.charId = charId;
	}
	
	public int getClanId() {
		return clanId;
	}
	
	public void setClanId(int clanId) {
		this.clanId = clanId;
	}
	
	public int getCurrentClass() {
		return classId;
	}
	
	public void setClassId(int classId) {
		this.classId = classId;
	}
	
	public double getCurrentHp() {
		return currentHp;
	}
	
	public void setCurrentHp(double currentHp) {
		this.currentHp = currentHp;
	}
	
	public double getCurrentMp() {
		return currentMp;
	}
	
	public void setCurrentMp(double currentMp) {
		this.currentMp = currentMp;
	}
	
	public long getDeleteTimer() {
		return deleteTimer;
	}
	
	public void setDeleteTimer(long deleteTimer) {
		this.deleteTimer = deleteTimer;
	}
	
	public long getLastAccess() {
		return lastAccess;
	}
	
	public void setLastAccess(long lastAccess) {
		this.lastAccess = lastAccess;
	}
	
	public long getExp() {
		return exp;
	}
	
	public void setExp(long exp) {
		this.exp = exp;
	}
	
	public int getFace() {
		return face;
	}
	
	public void setFace(int face) {
		this.face = face;
	}
	
	public int getHairColor() {
		return hairColor;
	}
	
	public void setHairColor(int hairColor) {
		this.hairColor = hairColor;
	}
	
	public int getHairStyle() {
		return hairStyle;
	}
	
	public void setHairStyle(int hairStyle) {
		this.hairStyle = hairStyle;
	}
	
	public int getPaperdollObjectId(int slot) {
		return paperdoll[slot][0];
	}
	
	public int getPaperdollItemId(int slot) {
		return paperdoll[slot][1];
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public int getMaxHp() {
		return maxHp;
	}
	
	public void setMaxHp(int maxHp) {
		this.maxHp = maxHp;
	}
	
	public int getMaxMp() {
		return maxMp;
	}
	
	public void setMaxMp(int maxMp) {
		this.maxMp = maxMp;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public L2PcTemplate getTemplate() {
		return template;
	}
	
	public void setTemplate(L2PcTemplate t) {
		template = t;
	}
	
	public int getSex() {
		return sex;
	}
	
	public void setSex(int sex) {
		this.sex = sex;
	}
	
	public long getSp() {
		return sp;
	}
	
	public void setSp(long sp) {
		this.sp = sp;
	}
	
	public int getEnchantEffect() {
		if (paperdoll[Inventory.PAPERDOLL_RHAND][2] > 0) {
			return paperdoll[Inventory.PAPERDOLL_RHAND][2];
		}
		return paperdoll[Inventory.PAPERDOLL_RHAND][2];
	}
	
	public void setReputation(int k) {
		reputation = k;
	}
	
	public int getReputation() {
		return reputation;
	}
	
	public void setAugmentationId(long augmentationId) {
		this.augmentationId = augmentationId;
	}
	
	public long getAugmentationId() {
		return augmentationId;
	}
	
	public void setPkKills(int PkKills) {
		this.pkKills = PkKills;
	}
	
	public int getPkKills() {
		return pkKills;
	}
	
	public void setPvPKills(int PvPKills) {
		this.pvpKills = PvPKills;
	}
	
	public int getPvPKills() {
		return pvpKills;
	}
	
	public int getTransformId() {
		return transformId;
	}
	
	public void setTransformId(int id) {
		transformId = id;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void setZ(int z) {
		this.z = z;
	}
	
	public boolean isShowingHat() {
		return showHat;
	}
	
	public void setShowHat(boolean showHat) {
		this.showHat = showHat;
	}
	
	public int getVitalityPoints() {
		return vitalityPoints;
	}
	
	public void setVitalityPoints(int vitalityPoints) {
		this.vitalityPoints = vitalityPoints;
	}
	
	public int getVitalityLevel() {
		return vitalityLevel;
	}
}
