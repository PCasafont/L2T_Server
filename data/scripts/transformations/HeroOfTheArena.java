package transformations;

import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.instancemanager.TransformationManager;
import l2tserver.gameserver.model.L2Transformation;

public class HeroOfTheArena extends L2Transformation
{
	private static final int[] SKILLS = {5491, 619};
	
	public HeroOfTheArena()
	{
		// id, colRadius, colHeight
		super(128, 18, 46.0);
	}
	
	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 128 || getPlayer().isCursedWeaponEquipped())
			return;
		transformedSkills();
	}
	
	public void transformedSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		
		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
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
				
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new HeroOfTheArena());
	}
}
