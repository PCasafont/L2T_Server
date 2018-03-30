package transformations;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.TransformationManager;
import l2server.gameserver.model.L2Transformation;

public class SiegeGolemSpirit extends L2Transformation {
	private static final int[] SKILLS = {15573, 15574, 15570, 15571, 5491, 619};

	public SiegeGolemSpirit() {
		// id, colRadius, colHeight
		super(148, 40, 68.0);
	}

	@Override
	public void onTransform() {
		if (getPlayer().getTransformationId() != 148 || getPlayer().isCursedWeaponEquipped()) {
			return;
		}

		transformedSkills();
	}

	public void transformedSkills() {
		for (int i : SKILLS) {
			getPlayer().addSkill(SkillTable.getInstance().getInfo(i, 1), false);
		}

		getPlayer().setTransformAllowedSkills(SKILLS);
	}

	@Override
	public void onUntransform() {
		removeSkills();
	}

	public void removeSkills() {
		for (int i : SKILLS) {
			getPlayer().removeSkill(SkillTable.getInstance().getInfo(i, 1), false);
		}

		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}

	public static void main(String[] args) {
		TransformationManager.getInstance().registerTransformation(new SiegeGolemSpirit());
	}
}
