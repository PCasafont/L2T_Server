package transformations;

import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.instancemanager.TransformationManager;
import l2tserver.gameserver.model.L2Transformation;

public class SpiritOfNabiarov extends L2Transformation
{
	private static final int[] SKILLS = {5491,619,11311,11312,11313,11314,11315,11299,11310};
	
	public SpiritOfNabiarov()
	{
		// id, colRadius, colHeight
		super(509, 25, 39.5);
	}
	
	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 509 || getPlayer().isCursedWeaponEquipped())
			return;
		
		transformedSkills();
	}
	
	public void transformedSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		
		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		int skillLevel = getPlayer().getSkillLevelHash(11267);
		
		if (skillLevel > 0)
		{	
			//Naviarope Strike
			getPlayer().addSkill(SkillTable.getInstance().getInfo(11311, skillLevel), false);
		
			//Naviarope Flame
			getPlayer().addSkill(SkillTable.getInstance().getInfo(11312, skillLevel), false);
		
			//Naviarope Explosion
			getPlayer().addSkill(SkillTable.getInstance().getInfo(11313, skillLevel), false);
		
			//Corpse Blast
			getPlayer().addSkill(SkillTable.getInstance().getInfo(11314, skillLevel), false);
		}
		
		//Mass Servitor Ultimate Defense
		getPlayer().addSkill(SkillTable.getInstance().getInfo(11310, 1), false);

		//Servitor Balance Life
		getPlayer().addSkill(SkillTable.getInstance().getInfo(11299, 1), false);
		
		//Summon Defense
		getPlayer().addSkill(SkillTable.getInstance().getInfo(11315, 1), false);
		
		getPlayer().setTransformAllowedSkills(SKILLS);
	}
	
	@Override
	public void onUntransform()
	{
		removeSkills();
	}
	
	public void removeSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		int skillLevel = getPlayer().getSkillLevelHash(11267);
		
		if (skillLevel > 0)
		{	
			//Naviarope Strike
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(11311, skillLevel), false);
		
			//Naviarope Flame
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(11312, skillLevel), false);
		
			//Naviarope Explosion
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(11313, skillLevel), false);
		
			//Corpse Blast
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(11314, skillLevel), false);
		}
		
		//Summon Defense
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(11315, 1), false);
		
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new SpiritOfNabiarov());
	}
}
