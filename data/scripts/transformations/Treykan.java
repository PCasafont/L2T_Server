package transformations;

import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.instancemanager.TransformationManager;
import l2tserver.gameserver.model.L2Transformation;

public class Treykan extends L2Transformation
{
	private static final int[] SKILLS = new int[]{619};
	
	public Treykan()
	{
		// id, colRadius, colHeight
		super(126, 25, 27.00);
	}
	
	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 126 || getPlayer().isCursedWeaponEquipped())
			return;
		
		transformedSkills();
	}
	
	public void transformedSkills()
	{
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
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Treykan());
	}
}
