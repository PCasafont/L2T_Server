package transformations;

import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.instancemanager.TransformationManager;
import l2tserver.gameserver.model.L2Transformation;

public class TamePrincessAnt extends L2Transformation
{
	private static final int[] SKILLS = {5491,839};
	
	public TamePrincessAnt()
	{
		// id, colRadius, colHeight
		super(20008, 31, 38.0);
	}
	
	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 20008 || getPlayer().isCursedWeaponEquipped())
			return;
		transformedSkills();
	}
	
	public void transformedSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Dismount
		getPlayer().addSkill(SkillTable.getInstance().getInfo(839, 1), false);
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
		// Dismount
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(839, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new TamePrincessAnt());
	}
}
